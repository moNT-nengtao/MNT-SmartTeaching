package com.smartteaching.service.attendance;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.constant.AttendanceStatus;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.attendance.AttendanceCheckinDTO;
import com.smartteaching.common.dto.attendance.AttendanceSessionCreateDTO;
import com.smartteaching.common.dto.attendance.AttendanceStatusUpdateDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.utils.RedisUtils;
import com.smartteaching.common.utils.SecurityUtils;
import com.smartteaching.common.vo.attendance.AttendanceCourseVO;
import com.smartteaching.common.vo.attendance.AttendanceCurrentVO;
import com.smartteaching.common.vo.attendance.AttendanceExportVO;
import com.smartteaching.common.vo.attendance.AttendanceRecordVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionDetailVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionVO;
import com.smartteaching.common.vo.attendance.AttendanceStatsVO;
import com.smartteaching.common.vo.attendance.AttendanceStudentVO;
import com.smartteaching.entity.attendance.AttendanceRecord;
import com.smartteaching.entity.attendance.AttendanceSession;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.user.User;
import com.smartteaching.entity.warning.Warning;
import com.smartteaching.mapper.AttendanceMapper;
import com.smartteaching.mapper.AttendanceRecordMapper;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.UserMapper;
import com.smartteaching.mapper.WarningMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName AttendanceServiceImpl
 * @Description 考勤签到服务实现
 * 核心机制：
 * 1. 创建会话：会话+本课程全部学生考勤记录(初始缺勤)一次写入数据库，Redis 记录时效（上限20分钟）
 * 2. 学生签到：比对 Redis 活跃会话与图案，匹配则更新数据库状态为考勤成功
 * 3. 教师改状态：仅允许改为迟到/请假，不允许改为考勤成功
 * 4. 结束/超时：缺勤落定为旷课，联动生成/刷新学业预警中的旷课警告
 * @Author MNT
 * @Date 2026/9/2
 **/
@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    /** 签到时长上限（分钟） */
    private static final int MAX_DURATION_MINUTES = 20;

    /** Redis 会话信息 key：attendance:session:{sessionId} */
    private static final String SESSION_KEY_PREFIX = "attendance:session:";
    /** Redis 学生活跃会话集合 key：attendance:student:{studentId} */
    private static final String STUDENT_SET_KEY_PREFIX = "attendance:student:";
    /** Redis 教师当前会话 key：attendance:teacher:{teacherId}（同一教师同一时刻仅一个会话） */
    private static final String TEACHER_KEY_PREFIX = "attendance:teacher:";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AttendanceMapper attendanceMapper;

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private WarningMapper warningMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // =====================================================
    // 1. 创建签到会话（教师）
    // =====================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceSessionVO createSession(AttendanceSessionCreateDTO dto) {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);

        // 同一教师同一时刻仅允许一个签到会话：已有活跃会话则拒绝，须先结束当前签到
        String activeSessionId = stringRedisTemplate.opsForValue().get(TEACHER_KEY_PREFIX + currentUser.getId());
        if (activeSessionId != null && !activeSessionId.isEmpty()) {
            if (readSessionCache(Long.valueOf(activeSessionId)) != null) {
                throw new BaseException("您已有进行中的签到会话，请先结束当前签到后再发起新的签到");
            }
            // Redis 残留的失效 key，顺手清理
            stringRedisTemplate.delete(TEACHER_KEY_PREFIX + currentUser.getId());
        }

        // 校验课程
        if (dto.getCourseId() == null) {
            throw new BaseException("请选择课程");
        }
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || (course.getStatus() != null && course.getStatus() == 0)) {
            throw new BaseException(MessageConstant.ATTENDANCE_COURSE_NOT_EXIST);
        }
        // 教师只能为自己授课的课程发起签到
        if ("teacher".equals(currentUser.getRole())
                && (course.getTeacherId() == null || !course.getTeacherId().equals(currentUser.getId()))) {
            throw new BaseException(MessageConstant.ATTENDANCE_COURSE_NOT_OWNED);
        }

        // 校验图案
        String pattern = validateAndFormatPattern(dto.getPattern());

        // 校验时长：默认10分钟，强制上限20分钟
        int duration = dto.getDuration() == null ? 10 : dto.getDuration();
        if (duration < 1) {
            duration = 1;
        }
        if (duration > MAX_DURATION_MINUTES) {
            duration = MAX_DURATION_MINUTES;
        }

        // 获取本课程全部有效选课学生
        List<Long> studentIds = attendanceMapper.selectStudentIdsByCourse(course.getId());
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BaseException(MessageConstant.ATTENDANCE_NO_STUDENTS);
        }

        // 创建会话
        AttendanceSession session = new AttendanceSession();
        session.setCourseId(course.getId());
        session.setTeacherId(currentUser.getId());
        session.setClassId(null);
        session.setSessionDate(LocalDate.now());
        session.setStartTime(dto.getStartTime() != null ? dto.getStartTime() : LocalTime.now());
        session.setEndTime(dto.getEndTime());
        session.setCheckCode(pattern);
        session.setDuration(duration);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setStatus(1);
        attendanceMapper.insert(session);
        Long sessionId = session.getId();
        log.info("创建签到会话成功，sessionId={}, courseId={}, teacherId={}, duration={}分钟",
                sessionId, course.getId(), currentUser.getId(), duration);

        // 批量写入本课程学生考勤记录（初始缺勤0）——签到会话仅此一次向数据库写入考勤记录
        List<AttendanceRecord> records = new ArrayList<>();
        for (Long studentId : studentIds) {
            AttendanceRecord record = new AttendanceRecord();
            record.setSessionId(sessionId);
            record.setStudentId(studentId);
            record.setStatus(AttendanceStatus.ABSENT);
            record.setCheckinTime(null);
            records.add(record);
        }
        attendanceRecordMapper.batchInsertRecords(records);
        log.info("签到会话[{}]批量写入{}条考勤记录（初始缺勤）", sessionId, records.size());

        // 写入 Redis 时效：会话信息 + 每个学生的活跃会话集合
        writeSessionToRedis(session, pattern, duration, studentIds);

        return buildSessionVO(session);
    }

    // =====================================================
    // 2. 学生签到
    // =====================================================
    @Override
    public Map<String, Object> submitCheckin(AttendanceCheckinDTO dto) {
        User currentUser = getCurrentUser();
        if (!"student".equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.ATTENDANCE_NOT_STUDENT);
        }
        if (dto.getPattern() == null || dto.getPattern().size() < 3) {
            throw new BaseException(MessageConstant.ATTENDANCE_PATTERN_INVALID);
        }
        String inputPattern = dto.getPattern().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 获取该学生当前活跃会话
        Set<String> activeSessionIds = stringRedisTemplate.opsForSet()
                .members(STUDENT_SET_KEY_PREFIX + currentUser.getId());
        if (activeSessionIds == null || activeSessionIds.isEmpty()) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_EXPIRED);
        }

        // 比对图案
        for (String sessionIdStr : activeSessionIds) {
            Long sessionId = Long.valueOf(sessionIdStr);
            SessionCache cache = readSessionCache(sessionId);
            if (cache == null) {
                continue;
            }
            if (!inputPattern.equals(cache.getPattern())) {
                continue;
            }

            // 图案匹配，校验数据库会话仍为进行中
            AttendanceSession session = attendanceMapper.selectById(sessionId);
            if (session == null || session.getStatus() == null || session.getStatus() != 1) {
                throw new BaseException(MessageConstant.ATTENDANCE_SESSION_EXPIRED);
            }

            // 查询该学生在本次会话的考勤记录
            AttendanceRecord record = attendanceRecordMapper.selectOne(
                    Wrappers.<AttendanceRecord>lambdaQuery()
                            .eq(AttendanceRecord::getSessionId, sessionId)
                            .eq(AttendanceRecord::getStudentId, currentUser.getId())
                            .last("LIMIT 1"));
            if (record == null) {
                throw new BaseException(MessageConstant.ATTENDANCE_SESSION_EXPIRED);
            }

            // 依据当前状态分支处理
            int rows = attendanceRecordMapper.checkinSuccess(
                    record.getId(), sessionId, currentUser.getId(),
                    LocalDateTime.now(), dto.getLongitude(), dto.getLatitude());
            if (rows > 0) {
                Course course = courseMapper.selectById(session.getCourseId());
                log.info("学生[{}]在会话[{}]签到成功", currentUser.getId(), sessionId);
                Map<String, Object> result = new HashMap<>();
                result.put("sessionId", sessionId);
                result.put("courseId", session.getCourseId());
                result.put("courseName", course != null ? course.getName() : "");
                result.put("status", AttendanceStatus.PRESENT);
                return result;
            }

            // 影响行数为0，说明状态已被其他操作修改，重查判断
            AttendanceRecord latest = attendanceRecordMapper.selectById(record.getId());
            if (latest != null && latest.getStatus() != null) {
                if (latest.getStatus() == AttendanceStatus.PRESENT) {
                    throw new BaseException(MessageConstant.ATTENDANCE_ALREADY_CHECKED);
                }
                if (latest.getStatus() == AttendanceStatus.LATE || latest.getStatus() == AttendanceStatus.LEAVE) {
                    throw new BaseException(String.format(
                            MessageConstant.ATTENDANCE_MARKED_TPL,
                            AttendanceStatus.text(latest.getStatus())));
                }
            }
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_EXPIRED);
        }

        throw new BaseException(MessageConstant.ATTENDANCE_PATTERN_MISMATCH);
    }

    // =====================================================
    // 3. 会话详情（教师查看名单/倒计时/统计）
    // =====================================================
    @Override
    public AttendanceSessionDetailVO getSessionDetail(Long sessionId) {
        User currentUser = getCurrentUser();
        AttendanceSession session = checkSessionAccess(sessionId, currentUser);

        // 懒加载兜底：超时未结束的会话自动落定
        finalizeIfExpired(session);

        AttendanceSessionDetailVO detail = new AttendanceSessionDetailVO();
        detail.setSession(buildSessionVO(session));

        List<AttendanceStudentVO> records = attendanceMapper.selectSessionRecords(sessionId);
        if (records != null) {
            for (AttendanceStudentVO vo : records) {
                vo.setStatusText(AttendanceStatus.text(vo.getStatus()));
            }
        }
        detail.setRecords(records == null ? new ArrayList<>() : records);

        AttendanceStatsVO stats = attendanceMapper.selectSessionStats(sessionId);
        fillRate(stats);
        detail.setStats(stats == null ? new AttendanceStatsVO() : stats);
        return detail;
    }

    // =====================================================
    // 4. 教师修改考勤状态（迟到/请假/旷课）
    // =====================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(AttendanceStatusUpdateDTO dto) {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);
        if (dto.getRecordId() == null) {
            throw new BaseException("考勤记录ID不能为空");
        }
        if (dto.getStatus() == null
                || (dto.getStatus() != AttendanceStatus.LATE
                && dto.getStatus() != AttendanceStatus.LEAVE
                && dto.getStatus() != AttendanceStatus.TRUANT)) {
            throw new BaseException(MessageConstant.ATTENDANCE_STATUS_INVALID);
        }

        AttendanceRecord record = attendanceRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BaseException(MessageConstant.ATTENDANCE_RECORD_NOT_EXIST);
        }
        AttendanceSession session = attendanceMapper.selectById(record.getSessionId());
        if (session == null) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_NOT_EXIST);
        }
        checkSessionOwner(session, currentUser);
        // 历史会话不允许修改状态
        if (session.getStatus() == null || session.getStatus() != 1) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_ENDED);
        }

        record.setStatus(dto.getStatus());
        record.setUpdateTime(LocalDateTime.now());
        attendanceRecordMapper.updateById(record);

        // 标记为旷课后联动刷新学业预警中的旷课警告
        if (dto.getStatus() == AttendanceStatus.TRUANT) {
            generateTruancyWarnings(session);
        }
        log.info("教师[{}]将会话[{}]中学生[{}]考勤状态修改为[{}]",
                currentUser.getId(), session.getId(), record.getStudentId(),
                AttendanceStatus.text(dto.getStatus()));
    }

    // =====================================================
    // 4.1 教师手动签到（特殊状态留痕：status=5，区别于考勤成功）
    // =====================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualCheckin(Long recordId) {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);
        if (recordId == null) {
            throw new BaseException("考勤记录ID不能为空");
        }
        AttendanceRecord record = attendanceRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BaseException(MessageConstant.ATTENDANCE_RECORD_NOT_EXIST);
        }
        AttendanceSession session = attendanceMapper.selectById(record.getSessionId());
        if (session == null) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_NOT_EXIST);
        }
        checkSessionOwner(session, currentUser);
        // 历史会话不允许手动签到
        if (session.getStatus() == null || session.getStatus() != 1) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_ENDED);
        }

        // 条件更新：仅当记录仍为缺勤(0)时置为手动签到(5)，并写入签到时间留痕
        int rows = attendanceRecordMapper.manualCheckin(
                record.getId(), record.getSessionId(), record.getStudentId(), LocalDateTime.now());
        if (rows <= 0) {
            AttendanceRecord latest = attendanceRecordMapper.selectById(record.getId());
            if (latest != null && latest.getStatus() != null && latest.getStatus() != AttendanceStatus.ABSENT) {
                throw new BaseException(String.format("该学生考勤状态已为%s，无法手动签到",
                        AttendanceStatus.text(latest.getStatus())));
            }
            throw new BaseException(MessageConstant.ATTENDANCE_MANUAL_CHECKIN_FAILED);
        }
        log.info("教师[{}]为会话[{}]中学生[{}]执行手动签到",
                currentUser.getId(), session.getId(), record.getStudentId());
    }

    // =====================================================
    // 5. 结束签到会话
    // =====================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSession(Long sessionId) {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);
        AttendanceSession session = attendanceMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_NOT_EXIST);
        }
        checkSessionOwner(session, currentUser);
        if (session.getStatus() != null && session.getStatus() == 0) {
            throw new BaseException(MessageConstant.ATTENDANCE_ALREADY_ENDED);
        }

        // 清理 Redis（先清时效，阻断后续签到）
        cleanupSessionRedis(sessionId);

        // 落定：缺勤(0) -> 旷课(4)，会话标记结束
        attendanceRecordMapper.batchFinalizeTruant(sessionId);
        session.setStatus(0);
        session.setUpdateTime(LocalDateTime.now());
        attendanceMapper.updateById(session);

        // 联动生成/刷新学业预警中的旷课警告
        generateTruancyWarnings(session);
        log.info("会话[{}]已结束，缺勤记录已落定为旷课", sessionId);
    }

    // =====================================================
    // 6. 学生个人考勤记录
    // =====================================================
    @Override
    public List<AttendanceRecordVO> getMyAttendance() {
        User currentUser = getCurrentUser();
        List<AttendanceRecordVO> list = attendanceMapper.selectMyRecords(currentUser.getId());
        if (list != null) {
            for (AttendanceRecordVO vo : list) {
                vo.setStatusText(AttendanceStatus.text(vo.getStatus()));
            }
        }
        return list == null ? new ArrayList<>() : list;
    }

    // =====================================================
    // 7. 教师/学生当前活跃会话（进入页面恢复/展示用）
    // =====================================================
    @Override
    public AttendanceSessionVO getTeacherCurrentSession() {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);
        String activeId = stringRedisTemplate.opsForValue().get(TEACHER_KEY_PREFIX + currentUser.getId());
        if (activeId == null || activeId.isEmpty()) {
            return null;
        }
        AttendanceSession session = attendanceMapper.selectById(Long.valueOf(activeId));
        if (session == null || session.getStatus() == null || session.getStatus() != 1) {
            return null;
        }
        return buildSessionVO(session);
    }

    @Override
    public AttendanceCurrentVO getStudentCurrentSession() {
        User currentUser = getCurrentUser();
        if (!"student".equals(currentUser.getRole())) {
            throw new BaseException(MessageConstant.ATTENDANCE_NOT_STUDENT);
        }
        Set<String> activeSessionIds = stringRedisTemplate.opsForSet()
                .members(STUDENT_SET_KEY_PREFIX + currentUser.getId());
        if (activeSessionIds == null || activeSessionIds.isEmpty()) {
            return null;
        }
        for (String sessionIdStr : activeSessionIds) {
            Long sessionId = Long.valueOf(sessionIdStr);
            SessionCache cache = readSessionCache(sessionId);
            if (cache == null) {
                continue;
            }
            AttendanceSession session = attendanceMapper.selectById(sessionId);
            if (session == null || session.getStatus() == null || session.getStatus() != 1) {
                continue;
            }
            AttendanceCurrentVO vo = new AttendanceCurrentVO();
            vo.setSessionId(sessionId);
            vo.setCourseId(session.getCourseId());
            vo.setDuration(session.getDuration());
            vo.setRemainingSeconds(calcRemainingSeconds(session));
            Course course = courseMapper.selectById(session.getCourseId());
            vo.setCourseName(course != null ? course.getName() : "");
            User teacher = userMapper.selectById(session.getTeacherId());
            vo.setTeacherName(teacher != null ? teacher.getRealName() : "");
            AttendanceRecord record = attendanceRecordMapper.selectOne(
                    Wrappers.<AttendanceRecord>lambdaQuery()
                            .eq(AttendanceRecord::getSessionId, sessionId)
                            .eq(AttendanceRecord::getStudentId, currentUser.getId())
                            .last("LIMIT 1"));
            Integer st = record != null ? record.getStatus() : null;
            vo.setStatus(st);
            vo.setStatusText(AttendanceStatus.text(st));
            // 到场类（考勤成功/手动签到/迟到）视为已处理，不再提示待签到
            vo.setCheckedIn(st != null && (st == AttendanceStatus.PRESENT
                    || st == AttendanceStatus.MANUAL
                    || st == AttendanceStatus.LATE));
            return vo;
        }
        return null;
    }

    // =====================================================
    // 7.1 教师可发起签到的课程选项
    // =====================================================
    @Override
    public List<AttendanceCourseVO> getTeacherCourses() {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);
        Long teacherId = "admin".equals(currentUser.getRole()) ? null : currentUser.getId();
        return attendanceMapper.selectTeacherCourses(teacherId);
    }

    // =====================================================
    // 7.2 历史考勤会话列表（教师/管理员，名单切换用）
    // =====================================================
    @Override
    public List<AttendanceSessionVO> getSessionList(Long courseId) {
        User currentUser = getCurrentUser();
        checkTeacherPermission(currentUser);
        Long teacherId = "admin".equals(currentUser.getRole()) ? null : currentUser.getId();
        List<AttendanceSessionVO> list = attendanceMapper.selectSessionList(teacherId, courseId, 100);
        return list == null ? new ArrayList<>() : list;
    }

    // =====================================================
    // 8. 导出考勤报表
    // =====================================================
    @Override
    public void exportSession(Long sessionId, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser();
        AttendanceSession session = checkSessionAccess(sessionId, currentUser);
        finalizeIfExpired(session);

        List<AttendanceStudentVO> records = attendanceMapper.selectSessionRecords(sessionId);
        List<AttendanceExportVO> exportList = new ArrayList<>();
        if (records != null) {
            for (AttendanceStudentVO vo : records) {
                AttendanceExportVO item = new AttendanceExportVO();
                item.setStudentNo(vo.getStudentNo());
                item.setStudentName(vo.getStudentName());
                item.setClassName(vo.getClassName());
                item.setStatusText(AttendanceStatus.text(vo.getStatus()));
                item.setCheckinTime(vo.getCheckinTime() != null
                        ? vo.getCheckinTime().format(DATE_TIME_FORMATTER) : "");
                exportList.add(item);
            }
        }

        String fileName = URLEncoder.encode("考勤记录_" + sessionId + "_"
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx",
                StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        if (exportList.isEmpty()) {
            // 空数据也输出带表头的空表，避免前端下载到非Excel内容
            EasyExcel.write(response.getOutputStream(), AttendanceExportVO.class)
                    .autoCloseStream(Boolean.TRUE)
                    .sheet("考勤记录")
                    .doWrite(new ArrayList<AttendanceExportVO>());
            response.getOutputStream().flush();
            return;
        }
        EasyExcel.write(response.getOutputStream(), AttendanceExportVO.class)
                .autoCloseStream(Boolean.TRUE)
                .sheet("考勤记录")
                .doWrite(exportList);
        response.getOutputStream().flush();
    }

    // =====================================================
    // 9. 定时/兜底结束超时会话
    // =====================================================
    @Override
    public void finalizeExpiredSessions() {
        List<AttendanceSession> activeSessions = attendanceMapper.selectList(
                Wrappers.<AttendanceSession>lambdaQuery()
                        .eq(AttendanceSession::getStatus, 1)
                        .isNotNull(AttendanceSession::getDuration));
        LocalDateTime now = LocalDateTime.now();
        for (AttendanceSession session : activeSessions) {
            if (isExpired(session, now)) {
                try {
                    log.info("定时任务结束超时会话[{}]", session.getId());
                    // 清理Redis并落定旷课 + 生成预警
                    cleanupSessionRedis(session.getId());
                    attendanceRecordMapper.batchFinalizeTruant(session.getId());
                    session.setStatus(0);
                    session.setUpdateTime(now);
                    attendanceMapper.updateById(session);
                    generateTruancyWarnings(session);
                } catch (Exception e) {
                    log.error("定时结束会话[{}]失败", session.getId(), e);
                }
            }
        }
    }

    // =====================================================
    // 私有方法
    // =====================================================

    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new BaseException(MessageConstant.USER_NOT_LOGIN);
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            throw new BaseException("当前用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BaseException(MessageConstant.ACCOUNT_DISABLED);
        }
        return user;
    }

    private void checkTeacherPermission(User user) {
        if (!"teacher".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            throw new BaseException(MessageConstant.ATTENDANCE_NOT_TEACHER);
        }
    }

    /**
     * 校验图案：至少3个不同节点，取值0-8
     */
    private String validateAndFormatPattern(List<Integer> pattern) {
        if (pattern == null || pattern.size() < 3) {
            throw new BaseException(MessageConstant.ATTENDANCE_PATTERN_INVALID);
        }
        Set<Integer> distinct = new HashSet<>();
        for (Integer p : pattern) {
            if (p == null || p < 0 || p > 8) {
                throw new BaseException(MessageConstant.ATTENDANCE_PATTERN_INVALID);
            }
            distinct.add(p);
        }
        if (distinct.size() < 3) {
            throw new BaseException(MessageConstant.ATTENDANCE_PATTERN_INVALID);
        }
        return pattern.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 写入 Redis：会话信息(key=attendance:session:{id}) + 学生活跃会话集合(key=attendance:student:{studentId})
     */
    private void writeSessionToRedis(AttendanceSession session, String pattern, int duration,
                                     List<Long> studentIds) {
        SessionCache cache = new SessionCache();
        cache.setSessionId(session.getId());
        cache.setCourseId(session.getCourseId());
        cache.setTeacherId(session.getTeacherId());
        cache.setPattern(pattern);
        redisUtils.setStr(SESSION_KEY_PREFIX + session.getId(),
                JSON.toJSONString(cache), duration, TimeUnit.MINUTES);

        // 教师当前会话 key（同一教师同一时刻仅一个）
        stringRedisTemplate.opsForValue().set(
                TEACHER_KEY_PREFIX + session.getTeacherId(),
                String.valueOf(session.getId()), duration, TimeUnit.MINUTES);

        for (Long studentId : studentIds) {
            String key = STUDENT_SET_KEY_PREFIX + studentId;
            stringRedisTemplate.opsForSet().add(key, String.valueOf(session.getId()));
            stringRedisTemplate.expire(key, duration, TimeUnit.MINUTES);
        }
    }

    private SessionCache readSessionCache(Long sessionId) {
        String json = redisUtils.getStr(SESSION_KEY_PREFIX + sessionId);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(json, SessionCache.class);
        } catch (Exception e) {
            log.warn("解析Redis会话缓存失败，sessionId={}", sessionId, e);
            return null;
        }
    }

    /**
     * 清理会话 Redis 数据（会话信息 + 各学生集合中的会话ID）
     */
    private void cleanupSessionRedis(Long sessionId) {
        try {
            AttendanceSession session = attendanceMapper.selectById(sessionId);
            if (session != null) {
                redisUtils.delete(TEACHER_KEY_PREFIX + session.getTeacherId());
            }
            redisUtils.delete(SESSION_KEY_PREFIX + sessionId);
            List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                    Wrappers.<AttendanceRecord>lambdaQuery()
                            .eq(AttendanceRecord::getSessionId, sessionId));
            Set<Long> studentIds = records.stream()
                    .map(AttendanceRecord::getStudentId)
                    .collect(Collectors.toSet());
            for (Long studentId : studentIds) {
                stringRedisTemplate.opsForSet().remove(STUDENT_SET_KEY_PREFIX + studentId,
                        String.valueOf(sessionId));
            }
        } catch (Exception e) {
            log.warn("清理会话[{}]Redis数据异常（不影响数据库落定）", sessionId, e);
        }
    }

    /**
     * 判断会话是否超时
     */
    private boolean isExpired(AttendanceSession session, LocalDateTime now) {
        if (session.getStatus() == null || session.getStatus() != 1) {
            return true;
        }
        if (session.getDuration() == null || session.getCreateTime() == null) {
            return false;
        }
        return !session.getCreateTime().plusMinutes(session.getDuration()).isAfter(now);
    }

    /**
     * 懒加载兜底：超时会话自动落定
     */
    private void finalizeIfExpired(AttendanceSession session) {
        if (session == null || session.getStatus() == null || session.getStatus() != 1) {
            return;
        }
        if (!isExpired(session, LocalDateTime.now())) {
            return;
        }
        try {
            cleanupSessionRedis(session.getId());
            attendanceRecordMapper.batchFinalizeTruant(session.getId());
            session.setStatus(0);
            session.setUpdateTime(LocalDateTime.now());
            attendanceMapper.updateById(session);
            generateTruancyWarnings(session);
            log.info("懒加载兜底结束超时会话[{}]", session.getId());
        } catch (Exception e) {
            log.error("懒加载结束会话[{}]失败", session.getId(), e);
        }
    }

    /**
     * 校验会话访问权限：教师仅本人会话，管理员可访问全部；返回会话
     */
    private AttendanceSession checkSessionAccess(Long sessionId, User currentUser) {
        checkTeacherPermission(currentUser);
        AttendanceSession session = attendanceMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException(MessageConstant.ATTENDANCE_SESSION_NOT_EXIST);
        }
        checkSessionOwner(session, currentUser);
        return session;
    }

    private void checkSessionOwner(AttendanceSession session, User currentUser) {
        if ("admin".equals(currentUser.getRole())) {
            return;
        }
        if (session.getTeacherId() != null && session.getTeacherId().equals(currentUser.getId())) {
            return;
        }
        throw new BaseException(MessageConstant.ATTENDANCE_NO_PERMISSION);
    }

    private AttendanceSessionVO buildSessionVO(AttendanceSession session) {
        AttendanceSessionVO vo = new AttendanceSessionVO();
        vo.setSessionId(session.getId());
        vo.setCourseId(session.getCourseId());
        vo.setTeacherId(session.getTeacherId());
        vo.setSessionDate(session.getSessionDate());
        vo.setDuration(session.getDuration());
        vo.setStatus(session.getStatus());

        Course course = courseMapper.selectById(session.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        User teacher = userMapper.selectById(session.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }
        // 剩余秒数：优先取 Redis TTL，Redis 丢失时按墙钟推算
        vo.setRemainingSeconds(calcRemainingSeconds(session));
        // 回填九宫格图案（前端会话期间保留图案展示）
        SessionCache cache = readSessionCache(session.getId());
        if (cache != null && cache.getPattern() != null) {
            vo.setPattern(java.util.Arrays.stream(cache.getPattern().split(","))
                    .filter(s -> !s.isEmpty())
                    .map(Integer::valueOf)
                    .collect(java.util.stream.Collectors.toList()));
        }
        return vo;
    }

    /**
     * 计算会话剩余有效秒数
     */
    private Long calcRemainingSeconds(AttendanceSession session) {
        if (session.getStatus() == null || session.getStatus() != 1) {
            return 0L;
        }
        Long expire = redisUtils.getExpire(SESSION_KEY_PREFIX + session.getId());
        if (expire != null && expire > 0) {
            return expire;
        }
        if (session.getCreateTime() != null && session.getDuration() != null) {
            long remaining = session.getCreateTime().plusMinutes(session.getDuration())
                    .atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
                    - LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
            return Math.max(0, remaining);
        }
        return 0L;
    }

    private void fillRate(AttendanceStatsVO stats) {
        if (stats == null) {
            return;
        }
        long total = stats.getTotal() == null ? 0 : stats.getTotal();
        long present = stats.getPresent() == null ? 0 : stats.getPresent();
        long late = stats.getLate() == null ? 0 : stats.getLate();
        long manual = stats.getManual() == null ? 0 : stats.getManual();
        if (total == 0) {
            stats.setRate(0.0);
        } else {
            stats.setRate(Math.round((present + late + manual) * 10000.0 / total) / 100.0);
        }
    }

    /**
     * 生成/刷新学业预警中的旷课警告：以旷课(4)记录为准，累加次数，升级预警等级
     */
    private void generateTruancyWarnings(AttendanceSession session) {
        List<AttendanceRecord> truantRecords = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getSessionId, session.getId())
                        .eq(AttendanceRecord::getStatus, AttendanceStatus.TRUANT));
        if (truantRecords == null || truantRecords.isEmpty()) {
            return;
        }
        Course course = courseMapper.selectById(session.getCourseId());
        String courseName = course != null ? course.getName() : "未知课程";
        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecord record : truantRecords) {
            try {
                Long studentId = record.getStudentId();
                Long truantCount = attendanceRecordMapper.selectCount(
                        Wrappers.<AttendanceRecord>lambdaQuery()
                                .eq(AttendanceRecord::getStudentId, studentId)
                                .eq(AttendanceRecord::getStatus, AttendanceStatus.TRUANT));
                truantCount = truantCount == null ? 1 : Math.max(truantCount, 1);

                int level = truantCount >= 4 ? 3 : (truantCount >= 2 ? 2 : 1);
                String title = MessageConstant.WARNING_TYPE_ABSENT;
                User student = userMapper.selectById(studentId);
                String studentName = student != null ? student.getRealName() : ("学号" + studentId);
                String content = String.format(
                        "%s 在《%s》课堂被记录为旷课，当前累计旷课 %d 次。请严格遵守考勤制度，如有特殊情况请及时请假。",
                        studentName, courseName, truantCount);

                // 幂等：存在未处理的旷课预警则更新，否则新建
                Warning existing = warningMapper.selectOne(
                        Wrappers.<Warning>lambdaQuery()
                                .eq(Warning::getUserId, studentId)
                                .eq(Warning::getWarningType, "absent")
                                .eq(Warning::getStatus, 1)
                                .last("LIMIT 1"));
                if (existing != null) {
                    existing.setLevel(level);
                    existing.setTitle(title);
                    existing.setContent(content);
                    existing.setUpdateTime(now);
                    warningMapper.updateById(existing);
                } else {
                    Warning warning = new Warning();
                    warning.setUserId(studentId);
                    warning.setWarningType("absent");
                    warning.setLevel(level);
                    warning.setTitle(title);
                    warning.setContent(content);
                    warning.setStatus(1);
                    warning.setCreateTime(now);
                    warning.setUpdateTime(now);
                    warningMapper.insert(warning);
                }
                log.info("旷课预警已生成/刷新，studentId={}, 累计旷课={}次, level={}", studentId, truantCount, level);
            } catch (Exception e) {
                log.error("生成旷课预警失败，studentId={}", record.getStudentId(), e);
            }
        }
    }

    /**
     * Redis 会话缓存对象
     */
    public static class SessionCache {
        private Long sessionId;
        private Long courseId;
        private Long teacherId;
        private String pattern;

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
}

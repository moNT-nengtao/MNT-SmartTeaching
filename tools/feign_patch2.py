# -*- coding: utf-8 -*-
"""第二批 Feign 改造：Notice / Attendance + 拷贝 CourseMapper 到 teaching-service"""
import io, sys, shutil, os
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

ROOT = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
ok_count = 0
fail_list = []

def patch(path, old, new, count=1):
    global ok_count
    p = ROOT + '\\' + path
    with open(p, encoding='utf-8') as f:
        c = f.read()
    n = c.count(old)
    if n != count:
        fail_list.append((path, old[:60], 'expect %d got %d' % (count, n)))
        return
    c = c.replace(old, new)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(c)
    ok_count += 1
    print('OK  %s' % path)

# ==================== NoticeServiceImpl ====================
patch(r'teaching-service\src\main\java\com\smartteaching\service\notice\NoticeServiceImpl.java',
"""import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.NoticeMapper;""",
"""import com.smartteaching.api.client.CourseFeignClient;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.mapper.NoticeMapper;""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\notice\NoticeServiceImpl.java',
"""    @Resource
    private CourseMapper courseMapper;""",
"""    @Resource
    private CourseFeignClient courseFeignClient;""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\notice\NoticeServiceImpl.java',
"""            Course course = courseMapper.selectById(notice.getCourseId());""",
"""            CourseInfoVO course = courseFeignClient.getCourseById(notice.getCourseId()).getData();""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\notice\NoticeServiceImpl.java',
"""    private Course checkCourse(Long courseId, User currentUser) {""",
"""    private CourseInfoVO checkCourse(Long courseId, User currentUser) {""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\notice\NoticeServiceImpl.java',
"""        Course course = courseMapper.selectById(courseId);""",
"""        CourseInfoVO course = courseFeignClient.getCourseById(courseId).getData();""")

# ==================== AttendanceServiceImpl ====================
# --- imports ---
patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
"""import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.user.User;
import com.smartteaching.entity.warning.Warning;
import com.smartteaching.mapper.AttendanceMapper;
import com.smartteaching.mapper.AttendanceRecordMapper;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.UserMapper;
import com.smartteaching.mapper.WarningMapper;""",
"""import com.smartteaching.api.client.CourseFeignClient;
import com.smartteaching.api.client.UserFeignClient;
import com.smartteaching.api.client.WarningFeignClient;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.api.dto.TruantWarningDTO;
import com.smartteaching.api.dto.UserInfoVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.AttendanceMapper;
import com.smartteaching.mapper.AttendanceRecordMapper;
import com.smartteaching.mapper.UserMapper;""")

# --- 字段 ---
patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
"""    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private WarningMapper warningMapper;""",
"""    @Resource
    private CourseFeignClient courseFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private UserMapper userMapper;

    @Resource
    private WarningFeignClient warningFeignClient;""")

# --- 调用点 ---
patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
"""        Course course = courseMapper.selectById(dto.getCourseId());""",
"""        CourseInfoVO course = courseFeignClient.getCourseById(dto.getCourseId()).getData();""")

patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
"""                Course course = courseMapper.selectById(session.getCourseId());""",
"""                CourseInfoVO course = courseFeignClient.getCourseById(session.getCourseId()).getData();""")

patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
"""            Course course = courseMapper.selectById(session.getCourseId());
            vo.setCourseName(course != null ? course.getName() : "");
            User teacher = userMapper.selectById(session.getTeacherId());
            vo.setTeacherName(teacher != null ? teacher.getRealName() : "");""",
"""            CourseInfoVO course = courseFeignClient.getCourseById(session.getCourseId()).getData();
            vo.setCourseName(course != null ? course.getName() : "");
            UserInfoVO teacher = userFeignClient.getUserById(session.getTeacherId()).getData();
            vo.setTeacherName(teacher != null ? teacher.getRealName() : "");""")

patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
"""        Course course = courseMapper.selectById(session.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        User teacher = userMapper.selectById(session.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }""",
"""        CourseInfoVO course = courseFeignClient.getCourseById(session.getCourseId()).getData();
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        UserInfoVO teacher = userFeignClient.getUserById(session.getTeacherId()).getData();
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }""")

# --- generateTruancyWarnings 整体重写（幂等逻辑移交 warning-service）---
old_gen = """    private void generateTruancyWarnings(AttendanceSession session) {
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
    }"""

new_gen = """    private void generateTruancyWarnings(AttendanceSession session) {
        List<AttendanceRecord> truantRecords = attendanceRecordMapper.selectList(
                Wrappers.<AttendanceRecord>lambdaQuery()
                        .eq(AttendanceRecord::getSessionId, session.getId())
                        .eq(AttendanceRecord::getStatus, AttendanceStatus.TRUANT));
        if (truantRecords == null || truantRecords.isEmpty()) {
            return;
        }
        // 跨服务 Feign 调用 course-service 查询课程名
        CourseInfoVO course = courseFeignClient.getCourseById(session.getCourseId()).getData();
        String courseName = course != null ? course.getName() : "未知课程";

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
                // 跨服务 Feign 调用 uaa-service 查询学生姓名
                UserInfoVO student = userFeignClient.getUserById(studentId).getData();
                String studentName = student != null ? student.getRealName() : ("学号" + studentId);
                String content = String.format(
                        "%s 在《%s》课堂被记录为旷课，当前累计旷课 %d 次。请严格遵守考勤制度，如有特殊情况请及时请假。",
                        studentName, courseName, truantCount);

                // 跨服务 Feign 调用 warning-service 生成/刷新旷课预警（幂等 upsert 移交预警服务）
                warningFeignClient.createTruantWarning(new TruantWarningDTO(
                        studentId, studentName, courseName, truantCount, level, title, content));
                log.info("旷课预警已远程生成/刷新，studentId={}, 累计旷课={}次, level={}", studentId, truantCount, level);
            } catch (Exception e) {
                log.error("生成旷课预警失败，studentId={}", record.getStudentId(), e);
            }
        }
    }"""

patch(r'attendance-service\src\main\java\com\smartteaching\service\attendance\AttendanceServiceImpl.java',
     old_gen, new_gen)

# ==================== 拷贝 CourseMapper 到 teaching-service（selectTeacherCoursesWithStat 等定制 SQL 保留本地） ====================
src_java = ROOT + r'\course-service\src\main\java\com\smartteaching\mapper\CourseMapper.java'
dst_java = ROOT + r'\teaching-service\src\main\java\com\smartteaching\mapper\CourseMapper.java'
src_xml = ROOT + r'\course-service\src\main\resources\mapper\CourseMapper.xml'
dst_xml = ROOT + r'\teaching-service\src\main\resources\mapper\CourseMapper.xml'
for s, d in [(src_java, dst_java), (src_xml, dst_xml)]:
    if os.path.exists(d):
        print('SKIP copy (exists): %s' % d)
    else:
        shutil.copyfile(s, d)
        print('COPY -> %s' % d)

print('=' * 40)
print('patched: %d, failed: %d' % (ok_count, len(fail_list)))
for f in fail_list:
    print('FAIL', f)

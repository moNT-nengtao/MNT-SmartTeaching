package com.smartteaching.controller.attendance;

import com.smartteaching.common.dto.attendance.AttendanceCheckinDTO;
import com.smartteaching.common.dto.attendance.AttendanceSessionCreateDTO;
import com.smartteaching.common.dto.attendance.AttendanceStatusUpdateDTO;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.attendance.AttendanceCourseVO;
import com.smartteaching.common.vo.attendance.AttendanceCurrentVO;
import com.smartteaching.common.vo.attendance.AttendanceRecordVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionDetailVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionVO;
import com.smartteaching.common.vo.attendance.AttendanceStatsVO;
import com.smartteaching.service.attendance.AttendanceService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AttendanceController
 * @Description 签到考勤控制器
 * @Author MNT
 * @Date 2026/9/2
 **/
@RestController
@RequestMapping("/api/attendance")
@Slf4j
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    /**
     * 发起签到会话（教师）：一次写入本课程学生考勤记录 + Redis 时效（上限20分钟）
     */
    @PostMapping("/generate")
    public Result<AttendanceSessionVO> generateSession(@RequestBody AttendanceSessionCreateDTO dto) {
        log.info("发起签到会话: courseId={}, duration={}, pattern={}",
                dto.getCourseId(), dto.getDuration(), dto.getPattern());
        return Result.success(attendanceService.createSession(dto));
    }

    /**
     * 学生签到：比对 Redis 活跃会话与图案，匹配则落库为考勤成功
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitCheckin(@RequestBody AttendanceCheckinDTO dto) {
        log.info("学生签到提交");
        return Result.success(attendanceService.submitCheckin(dto));
    }

    /**
     * 签到会话详情（会话信息 + 学生名单 + 统计）
     */
    @GetMapping("/{sessionId}/list")
    public Result<AttendanceSessionDetailVO> getSessionList(@PathVariable Long sessionId) {
        log.info("签到会话详情: sessionId={}", sessionId);
        return Result.success(attendanceService.getSessionDetail(sessionId));
    }

    /**
     * 教师当前活跃签到会话（无则返回null，进入页面恢复展示用）
     */
    @GetMapping("/teacher/current")
    public Result<AttendanceSessionVO> getTeacherCurrentSession() {
        log.info("查询教师当前活跃签到会话");
        return Result.success(attendanceService.getTeacherCurrentSession());
    }

    /**
     * 学生当前待签到会话（无则返回null，展示当前需签到课程）
     */
    @GetMapping("/student/current")
    public Result<AttendanceCurrentVO> getStudentCurrentSession() {
        log.info("查询学生当前待签到会话");
        return Result.success(attendanceService.getStudentCurrentSession());
    }

    /**
     * 导出会话考勤报表
     */
    @GetMapping("/{sessionId}/export")
    public void exportSession(@PathVariable Long sessionId, HttpServletResponse response) throws IOException {
        log.info("导出会话考勤报表: sessionId={}", sessionId);
        attendanceService.exportSession(sessionId, response);
    }

    /**
     * 教师修改考勤状态（仅允许迟到/请假/旷课，历史会话不允许修改）
     */
    @PutMapping("/record/{recordId}/status")
    public Result updateStatus(@PathVariable Long recordId,
                               @RequestBody AttendanceStatusUpdateDTO dto) {
        dto.setRecordId(recordId);
        log.info("修改考勤状态: recordId={}, status={}", recordId, dto.getStatus());
        attendanceService.updateStatus(dto);
        return Result.success("考勤状态修改成功");
    }

    /**
     * 教师手动签到（特殊状态留痕：status=5，区别于考勤成功）
     */
    @PutMapping("/record/{recordId}/manual")
    public Result manualCheckin(@PathVariable Long recordId) {
        log.info("教师手动签到: recordId={}", recordId);
        attendanceService.manualCheckin(recordId);
        return Result.success("手动签到成功");
    }

    /**
     * 历史考勤会话列表（教师/管理员，用于名单切换）
     */
    @GetMapping("/sessions")
    public Result<List<AttendanceSessionVO>> getAttendanceSessions(
            @RequestParam(required = false) Long courseId) {
        log.info("查询历史考勤会话列表: courseId={}", courseId);
        return Result.success(attendanceService.getSessionList(courseId));
    }

    /**
     * 结束签到会话（缺勤落定为旷课并联动生成旷课预警）
     */
    @PostMapping("/{sessionId}/end")
    public Result endSession(@PathVariable Long sessionId) {
        log.info("结束签到会话: sessionId={}", sessionId);
        attendanceService.endSession(sessionId);
        return Result.success("签到会话已结束");
    }

    /**
     * 教师可发起签到的课程选项
     */
    @GetMapping("/courses")
    public Result<List<AttendanceCourseVO>> getTeacherCourses() {
        log.info("查询教师可签到课程");
        return Result.success(attendanceService.getTeacherCourses());
    }

    /**
     * 学生个人考勤记录
     */
    @GetMapping("/my")
    public Result<List<AttendanceRecordVO>> getMyAttendance() {
        log.info("查询个人考勤记录");
        return Result.success(attendanceService.getMyAttendance());
    }
}

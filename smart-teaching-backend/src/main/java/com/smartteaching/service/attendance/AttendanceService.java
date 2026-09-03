package com.smartteaching.service.attendance;

import com.smartteaching.common.dto.attendance.AttendanceCheckinDTO;
import com.smartteaching.common.dto.attendance.AttendanceSessionCreateDTO;
import com.smartteaching.common.dto.attendance.AttendanceStatusUpdateDTO;
import com.smartteaching.common.vo.attendance.AttendanceCourseVO;
import com.smartteaching.common.vo.attendance.AttendanceCurrentVO;
import com.smartteaching.common.vo.attendance.AttendanceRecordVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionDetailVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionVO;
import com.smartteaching.common.vo.attendance.AttendanceStatsVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AttendanceService
 * @Description 考勤签到服务接口
 * @Author MNT
 * @Date 2026/9/2
 **/
public interface AttendanceService {

    /**
     * 创建签到会话（教师）：写入会话 + 批量写入本课程学生考勤记录(初始缺勤) + 写入Redis时效
     */
    AttendanceSessionVO createSession(AttendanceSessionCreateDTO dto);

    /**
     * 学生签到：比对Redis中的活跃会话与图案，匹配则更新数据库状态为考勤成功
     */
    Map<String, Object> submitCheckin(AttendanceCheckinDTO dto);

    /**
     * 查询签到会话详情（会话信息 + 学生名单 + 统计）
     */
    AttendanceSessionDetailVO getSessionDetail(Long sessionId);

    /**
     * 教师修改考勤状态（仅允许迟到/请假/旷课）
     */
    void updateStatus(AttendanceStatusUpdateDTO dto);

    /**
     * 教师手动签到：学生到场但无法自主签到，教师代签为手动签到(5)（特殊状态留痕）
     */
    void manualCheckin(Long recordId);

    /**
     * 查询教师/管理员的历史考勤会话列表（用于名单切换，按时间倒序）
     *
     * @param courseId 可选按课程过滤，null查全部
     */
    List<AttendanceSessionVO> getSessionList(Long courseId);

    /**
     * 结束签到会话：缺勤落定为旷课、生成/刷新旷课预警、清理Redis
     */
    void endSession(Long sessionId);

    /**
     * 查询教师当前活跃签到会话（无则返回null，进入页面恢复展示用）
     */
    AttendanceSessionVO getTeacherCurrentSession();

    /**
     * 查询学生当前待签到会话（无则返回null，进入页面展示当前需签到课程）
     */
    AttendanceCurrentVO getStudentCurrentSession();

    /**
     * 查询学生个人考勤记录
     */
    List<AttendanceRecordVO> getMyAttendance();

    /**
     * 查询教师可发起签到的课程选项
     */
    List<AttendanceCourseVO> getTeacherCourses();

    /**
     * 导出会话考勤报表
     */
    void exportSession(Long sessionId, HttpServletResponse response) throws IOException;

    /**
     * 定时/兜底：结束已超时的签到会话（供定时任务与读取时懒加载调用）
     */
    void finalizeExpiredSessions();
}

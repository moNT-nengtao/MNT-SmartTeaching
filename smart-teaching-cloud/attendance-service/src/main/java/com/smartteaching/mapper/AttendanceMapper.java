package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.common.vo.attendance.AttendanceCourseVO;
import com.smartteaching.common.vo.attendance.AttendanceRecordVO;
import com.smartteaching.common.vo.attendance.AttendanceSessionVO;
import com.smartteaching.common.vo.attendance.AttendanceStatsVO;
import com.smartteaching.common.vo.attendance.AttendanceStudentVO;
import com.smartteaching.entity.attendance.AttendanceSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttendanceMapper extends BaseMapper<AttendanceSession> {

    /**
     * 查询签到会话详情（含课程名、教师名）
     */
    AttendanceSessionVO selectSessionDetail(@Param("sessionId") Long sessionId);

    /**
     * 查询会话全部学生考勤名单（含学生信息），用于教师端查看与修改
     */
    List<AttendanceStudentVO> selectSessionRecords(@Param("sessionId") Long sessionId);

    /**
     * 查询学生个人考勤记录（含课程信息）
     */
    List<AttendanceRecordVO> selectMyRecords(@Param("studentId") Long studentId);

    /**
     * 查询会话考勤统计
     */
    AttendanceStatsVO selectSessionStats(@Param("sessionId") Long sessionId);

    /**
     * 查询课程的有效选课学生ID列表（用于创建签到会话时批量写入考勤记录）
     */
    List<Long> selectStudentIdsByCourse(@Param("courseId") Long courseId);

    /**
     * 查询教师授课课程选项（管理员可传null查全部启用课程）
     */
    List<AttendanceCourseVO> selectTeacherCourses(@Param("teacherId") Long teacherId);

    /**
     * 查询教师/管理员的历史考勤会话列表（用于名单切换，按时间倒序）
     *
     * @param teacherId 教师ID，管理员传null查全部
     * @param courseId  课程ID，可选按课程过滤
     * @param limit     返回条数上限
     */
    List<AttendanceSessionVO> selectSessionList(@Param("teacherId") Long teacherId,
                                                @Param("courseId") Long courseId,
                                                @Param("limit") int limit);
}

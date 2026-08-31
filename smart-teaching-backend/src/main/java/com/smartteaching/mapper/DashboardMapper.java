package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.common.vo.dashborad.DashboardAdminVO;
import com.smartteaching.common.vo.dashborad.DashboardStudentVO;
import com.smartteaching.common.vo.dashborad.DashboardTeacherVO;
import com.smartteaching.entity.dashboard.DashboardStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper extends BaseMapper<DashboardStat> {
    //管理员
    /**
     * 获取统计卡片：班级、教师、学生、课程总数、选课率、考勤合格率
     */
    DashboardAdminVO.StatCards selectDashboardAdminCard();

    /**
     * 各学院学生人数分布
     * @return key:学院名称, value:学生数量
     */
    List<Map<String,Object>> selectCollegeStudentCount();

    /**
     * 师生总人数：teacherCount / studentCount
     */
    Map<String,Long> selectTeacherStudentTotal();

    /**
     * 近7日活跃用户(登录用户，取sys_user last_login_time)
     * @param dateList 近7天日期字符串 yyyy‑MM‑dd
     */
    List<Map<String,Object>> selectWeeklyActiveUser(@Param("dateList") List<String> dateList);

    //教师
    /**
     * 获取统计卡片
     * @return
     */
    DashboardTeacherVO.TeacherStatCards selectDashboardTeacherCard(Long teacherId);

    /**
     * 成绩区间分布统计
     * @param teacherId
     * @return
     */
    BigDecimal selectTeacherAvgEvaluate(Long teacherId);

    /**
     * 成绩分布柱状图
     *
     * @param teacherId
     * @return
     */
    Map<String, Object> selectTeacherScoreSegment(@Param("teacherId") Long teacherId);

    /**
     * 近7日考勤签到率趋势
     * @param teacherId
     * @return
     */
    List<Map<String, Object>> selectTeacherAttendanceTrend(@Param("teacherId") Long teacherId);

    //学生
    /**
     * 计算学生GPA
     */
    BigDecimal calculateGPA(@Param("studentId") Long studentId);

    /**
     * 计算已修学分（及格课程）
     */
    BigDecimal calculateFinishedCredit(@Param("studentId") Long studentId);

    /**
     * 统计挂科科目数
     */
    Integer countFailSubjects(@Param("studentId") Long studentId);

    /**
     * 获取学生各科成绩（用于雷达图）
     */
    List<Map<String, Object>> getStudentScores(@Param("studentId") Long studentId);

    /**
     * 获取学生成绩按学期分组（用于绩点趋势）
     */
    List<Map<String, Object>> getScoresBySemester(@Param("studentId") Long studentId);

    /**
     * 获取各科成绩详情（用于成绩对比柱状图）
     */
    List<Map<String, Object>> getScoreDetail(@Param("studentId") Long studentId);

    /**
     * 统计本月考勤率
     */
    BigDecimal calculateMonthlyAttendanceRate(@Param("studentId") Long studentId);

    /**
     * 获取月度考勤统计（近6个月）
     */
    List<Map<String, Object>> getMonthlyAttendance(@Param("studentId") Long studentId);

    /**
     * 获取学生信息（含班级、专业、学院）
     */
    Map<String, Object> getStudentInfo(@Param("userId") Long userId);
}

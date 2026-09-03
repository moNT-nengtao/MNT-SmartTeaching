package com.smartteaching.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @ClassName AiAnalysisMapper
 * @Description 学业分析数据查询：基于学生成绩、考勤、作业数据生成分析素材
 * @Author MNT
 * @Date 2026/9/2 23:15
 **/
@Mapper
public interface AiAnalysisMapper {

    /**
     * 学生成绩概况（课程数、平均分、最高、最低、及格数、不及格数）
     */
    @Select("SELECT COUNT(*) AS courseCount, " +
            "COALESCE(AVG(ss.score),0) AS avgScore, " +
            "COALESCE(MIN(ss.score),0) AS minScore, " +
            "COALESCE(MAX(ss.score),0) AS maxScore, " +
            "COALESCE(SUM(CASE WHEN ss.score >= 60 THEN 1 ELSE 0 END),0) AS passCount, " +
            "COALESCE(SUM(CASE WHEN ss.score < 60 THEN 1 ELSE 0 END),0) AS failCount " +
            "FROM student_score ss " +
            "WHERE ss.student_id = #{studentId} AND ss.status = 1 AND ss.score IS NOT NULL")
    Map<String, Object> selectScoreSummary(@Param("studentId") Long studentId);

    /**
     * 学生各科成绩明细（最近10门）
     */
    @Select("SELECT c.name AS courseName, c.semester AS semester, " +
            "ss.usual_score AS usualScore, ss.final_score AS finalScore, ss.score AS totalScore " +
            "FROM student_score ss " +
            "INNER JOIN course c ON ss.course_id = c.id " +
            "WHERE ss.student_id = #{studentId} AND ss.status = 1 AND ss.score IS NOT NULL AND c.status = 1 " +
            "ORDER BY ss.create_time DESC LIMIT 10")
    List<Map<String, Object>> selectScoreDetail(@Param("studentId") Long studentId);

    /**
     * 学生考勤概况（应到、出勤、迟到、请假、缺勤/旷课）
     */
    @Select("SELECT COUNT(*) AS totalSessions, " +
            "COALESCE(SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END),0) AS presentCount, " +
            "COALESCE(SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END),0) AS lateCount, " +
            "COALESCE(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END),0) AS leaveCount, " +
            "COALESCE(SUM(CASE WHEN status IN (0,4) THEN 1 ELSE 0 END),0) AS absentCount " +
            "FROM attendance_record " +
            "WHERE student_id = #{studentId}")
    Map<String, Object> selectAttendanceSummary(@Param("studentId") Long studentId);

    /**
     * 学生作业概况（已提交份数、已批改份数、作业平均分）
     */
    @Select("SELECT COUNT(*) AS submittedCount, " +
            "COALESCE(SUM(CASE WHEN score IS NOT NULL THEN 1 ELSE 0 END),0) AS gradedCount, " +
            "COALESCE(AVG(score),0) AS avgHomeworkScore " +
            "FROM homework_submission " +
            "WHERE student_id = #{studentId} AND status = 1")
    Map<String, Object> selectHomeworkSummary(@Param("studentId") Long studentId);
}

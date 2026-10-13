package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.common.vo.evaluation.EvaluationCourseVO;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherRankingVO;
import com.smartteaching.entity.evaluation.CourseEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationMapper extends BaseMapper<CourseEvaluation> {
    /**
     * 查询学生可评价课程列表（含评价状态、成绩状态）
     * @param studentId
     * @return
     */
    List<EvaluationCourseVO> selectEvaluableCourses(@Param("studentId") Long studentId);


    /**
     * 教师评分榜单
     * @param collegeId
     * @param subject
     * @return
     */
    List<EvaluationTeacherRankingVO> selectTeacherRanking(Long collegeId, String subject);
}

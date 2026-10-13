package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.common.vo.evaluation.EvaluationTeacherVO;
import com.smartteaching.entity.course.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程 Mapper（精简版，仅保留评价域聚合统计查询）
 * 课程域完整 Mapper 在 course-service；本服务仅通过共享库执行评价统计 SQL
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 查询教师课程列表（含选课人数、评价数、评分）
     * @param teacherId
     * @return
     */
    List<EvaluationTeacherVO.CourseItem> selectTeacherCoursesWithStat(@Param("teacherId") Long teacherId);
}

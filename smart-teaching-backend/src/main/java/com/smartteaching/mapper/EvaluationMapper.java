package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.entity.evaluation.CourseEvaluation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EvaluationMapper extends BaseMapper<CourseEvaluation> {
}

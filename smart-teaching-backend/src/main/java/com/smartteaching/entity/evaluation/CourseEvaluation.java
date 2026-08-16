package com.smartteaching.entity.evaluation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("course_evaluation")
public class CourseEvaluation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long teacherId;

    private Long studentId;

    private BigDecimal score;

    private String content;
}

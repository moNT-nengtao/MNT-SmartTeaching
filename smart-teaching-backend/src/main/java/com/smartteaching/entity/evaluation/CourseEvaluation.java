package com.smartteaching.entity.evaluation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName CourseEvaluation
 * @Description 课程评价实体类，对应course_evaluation表，存储学生对课程和教师的评价打分
 * @Author MNT
 * @Date 2026/8/14 16:12
 **/
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

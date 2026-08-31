package com.smartteaching.entity.evaluation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * 综合评分（1-5分）
     */
    private BigDecimal score;

    /**
     * 授课能力评分
     */
    private BigDecimal teachingAbility;

    /**
     * 课堂氛围评分
     */
    private BigDecimal classAtmosphere;

    /**
     * 知识讲解清晰度评分
     */
    private BigDecimal knowledgeClarity;

    /**
     * 作业批改反馈评分
     */
    private BigDecimal homeworkFeedback;

    /**
     * 答疑服务评分
     */
    private BigDecimal qaService;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 排除 updateTime，因为数据库表没有这个字段
     */
    @TableField(exist = false)
    private LocalDateTime updateTime;
}

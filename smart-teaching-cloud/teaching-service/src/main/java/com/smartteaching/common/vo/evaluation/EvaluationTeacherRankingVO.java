package com.smartteaching.common.vo.evaluation;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName EvaluationTeacherRankingVO
 * @Description
 * @Author MNT
 * @Date 2026/8/31 00:43
 **/
@Data
public class EvaluationTeacherRankingVO {

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 教师姓名
     */
    private String teacherName;

    /**
     * 所属学院名称
     */
    private String collegeName;

    /**
     * 授课数
     */
    private Integer courseCount;

    /**
     * 评价数
     */
    private Integer evaluationCount;

    /**
     * 综合评分（1-5分）
     */
    private BigDecimal avgScore;

    // ========== 各维度评分 ==========

    /**
     * 授课能力
     */
    private BigDecimal teachingAbility;

    /**
     * 课堂氛围
     */
    private BigDecimal classAtmosphere;

    /**
     * 知识讲解清晰度
     */
    private BigDecimal knowledgeClarity;

    /**
     * 作业批改反馈
     */
    private BigDecimal homeworkFeedback;

    /**
     * 答疑服务
     */
    private BigDecimal qaService;
}
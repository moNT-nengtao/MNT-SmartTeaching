package com.smartteaching.common.vo.score;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ScoreMyQueryVO
 * @Description 我的成绩返回VO
 * @Author MNT
 * @Date 2026/8/28 16:11
 **/
@Data
public class ScoreMyQueryVO {

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 学分
     */
    private BigDecimal credit;

    /**
     * 平时成绩
     */
    private BigDecimal usualScore;

    /**
     * 期末成绩
     */
    private BigDecimal finalScore;

    /**
     * 综合总评成绩
     */
    private BigDecimal totalScore;

    /**
     * 学期 例：2026‑2027‑1
     */
    private String semester;

    // 可选：课程id，用于跳转详情
    private Long courseId;

}
package com.smartteaching.common.vo.score;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ScoreAbnormalVO
 * @Description 异常成绩返回VO
 * @Author MNT
 * @Date 2026/8/27 12:09
 **/
@Data
public class ScoreAbnormalVO {

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 总评成绩
     */
    private BigDecimal totalScore;

    /**
     * 异常原因
     */
    private String reason;
}
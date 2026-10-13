package com.smartteaching.common.vo.score;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @ClassName ScoreCourseQueryVO
 * @Description
 * @Author MNT
 * @Date 2026/8/28 22:20
 **/
@Data
public class ScoreCourseQueryVO {
    /** 成绩记录ID */
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 学号 */
    private String studentNo;

    /** 学生姓名 */
    private String studentName;

    /** 班级名称 */
    private String className;

    /** 平时成绩 */
    private BigDecimal usualScore;

    /** 期末成绩 */
    private BigDecimal finalScore;

    /** 总评成绩（综合成绩） */
    private BigDecimal score;

    /** 备注 */
    private String remark;
}
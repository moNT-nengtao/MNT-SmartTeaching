package com.smartteaching.common.dto.score;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ScoreEnterDTO
 * @Description 成绩录入DTO
 * @Author MNT
 * @Date 2026/8/28 22:26
 **/
@Data
public class ScoreEnterDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "成绩列表不能为空")
    @Size(min = 1, message = "至少有一条成绩数据")
    private List<StudentScore> scores;

    /**
     * 学生成绩内部类
     */
    @Data
    public static class StudentScore {

        @NotNull(message = "学生ID不能为空")
        private Long studentId;

        /** 平时成绩 */
        private BigDecimal usualScore;

        /** 期末成绩 */
        private BigDecimal finalScore;

        /** 总评成绩 */
        private BigDecimal totalScore;

        /** 备注 */
        private String remark;
    }
}
package com.smartteaching.common.dto.homework;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName HomeworkGradeDTO
 * @Description 教师批改作业 DTO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkGradeDTO {

    private Long submissionId;

    private BigDecimal score;

    private String comment;
}

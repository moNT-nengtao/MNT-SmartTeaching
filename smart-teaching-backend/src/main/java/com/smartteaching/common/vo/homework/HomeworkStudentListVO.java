package com.smartteaching.common.vo.homework;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName HomeworkStudentListVO
 * @Description 学生 作业列表 VO（含提交状态和成绩）
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkStudentListVO {

    private Long id;

    private Long courseId;

    private String courseName;

    private String title;

    private String content;

    private String attachmentUrl;

    private String attachmentName;

    private LocalDateTime deadline;

    private LocalDateTime createTime;

    private Long submissionId;

    /**
     * 提交状态：1=已提交，0=未提交
     */
    private Integer submitStatus;

    private BigDecimal score;

    private String comment;

    private LocalDateTime submitTime;
}

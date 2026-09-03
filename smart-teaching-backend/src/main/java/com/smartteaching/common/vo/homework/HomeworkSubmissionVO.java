package com.smartteaching.common.vo.homework;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName HomeworkSubmissionVO
 * @Description 作业提交记录 VO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkSubmissionVO {

    private Long id;

    private Long homeworkId;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private String className;

    private String content;

    private String attachmentUrl;

    private String attachmentName;

    private BigDecimal score;

    private String comment;

    private LocalDateTime submitTime;

    private LocalDateTime gradeTime;
}

package com.smartteaching.entity.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName HomeworkSubmission
 * @Description 作业提交实体类，对应 homework_submission 表
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
@TableName("homework_submission")
public class HomeworkSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homeworkId;

    private Long studentId;

    private String content;

    private String attachmentUrl;

    private String attachmentName;

    private BigDecimal score;

    private String comment;

    private LocalDateTime submitTime;

    private LocalDateTime gradeTime;

    private Integer status;
}

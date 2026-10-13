package com.smartteaching.common.vo.homework;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName HomeworkListVO
 * @Description 教师/管理员 作业列表 VO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkListVO {

    private Long id;

    private Long courseId;

    private String courseName;

    private Long teacherId;

    private String teacherName;

    private String title;

    private String content;

    private String attachmentUrl;

    private String attachmentName;

    private LocalDateTime deadline;

    private Integer status;

    private LocalDateTime createTime;

    private Integer submissionCount;

    private Integer gradedCount;
}

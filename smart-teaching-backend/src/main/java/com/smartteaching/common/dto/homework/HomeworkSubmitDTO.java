package com.smartteaching.common.dto.homework;

import lombok.Data;

/**
 * @ClassName HomeworkSubmitDTO
 * @Description 学生提交作业 DTO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkSubmitDTO {

    private Long homeworkId;

    private String content;
}

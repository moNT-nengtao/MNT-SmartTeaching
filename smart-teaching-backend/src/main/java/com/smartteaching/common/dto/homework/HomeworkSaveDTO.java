package com.smartteaching.common.dto.homework;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @ClassName HomeworkSaveDTO
 * @Description 发布/编辑作业 DTO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkSaveDTO {

    private Long id;

    private Long courseId;

    private String title;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
}

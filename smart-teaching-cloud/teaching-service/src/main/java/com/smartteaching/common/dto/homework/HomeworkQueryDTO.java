package com.smartteaching.common.dto.homework;

import lombok.Data;

/**
 * @ClassName HomeworkQueryDTO
 * @Description 作业查询 DTO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkQueryDTO {

    private Integer pageNum;

    private Integer pageSize;

    private Long courseId;

    private Long teacherId;

    private String title;
}

package com.smartteaching.common.dto.qa;

import lombok.Data;

/**
 * @ClassName QaQueryDTO
 * @Description 答疑社区问题查询 DTO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class QaQueryDTO {

    private Integer page;

    private Integer pageSize;

    /**
     * 课程分区筛选
     */
    private Long courseId;

    /**
     * 标签筛选
     */
    private String tag;

    private String keyword;
}

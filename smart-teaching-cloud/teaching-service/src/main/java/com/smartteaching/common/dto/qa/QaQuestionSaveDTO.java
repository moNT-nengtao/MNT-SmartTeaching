package com.smartteaching.common.dto.qa;

import lombok.Data;

/**
 * @ClassName QaQuestionSaveDTO
 * @Description 发布问题 DTO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class QaQuestionSaveDTO {

    /**
     * 所属课程分区
     */
    private Long courseId;

    /**
     * 问题标签（单个，前端单选）
     */
    private String tag;

    private String title;

    private String content;

    /**
     * 是否匿名发布
     */
    private Boolean isAnonymous;
}

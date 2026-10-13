package com.smartteaching.common.vo.qa;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName QaReplyVO
 * @Description 问题回复 VO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class QaReplyVO {

    private Long id;

    private Long questionId;

    private Long userId;

    private String authorName;

    /**
     * 回复人是否为教师（用于前端展示"教师"标签）
     */
    private Boolean isTeacher;

    private String content;

    private Integer likeCount;

    private LocalDateTime createTime;
}

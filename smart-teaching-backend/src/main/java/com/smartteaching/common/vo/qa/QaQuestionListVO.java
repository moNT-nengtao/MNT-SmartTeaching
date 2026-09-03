package com.smartteaching.common.vo.qa;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName QaQuestionListVO
 * @Description 问题列表/详情 VO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class QaQuestionListVO {

    private Long id;

    private Long courseId;

    private String courseName;

    private String title;

    private String content;

    /**
     * 标签（单选）
     */
    private String tag;

    private Integer isTop;

    /**
     * 是否匿名：0=实名, 1=匿名
     */
    private Integer isAnonymous;

    private Long authorId;

    /**
     * 提问者姓名（匿名时为 null）
     */
    private String authorName;

    private Integer replyCount;

    private Integer likeCount;

    private LocalDateTime createTime;
}

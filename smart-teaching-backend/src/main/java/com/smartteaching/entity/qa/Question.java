package com.smartteaching.entity.qa;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName Question
 * @Description 问答提问实体类，对应qa_question表，管理用户发布的提问与互动统计
 * @Author MNT
 * @Date 2026/8/14 11:53
 **/
@Data
@TableName("qa_question")
public class Question extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 关联课程ID（课程分区）
     */
    private Long courseId;

    private String title;

    private String content;

    private String tags;

    /**
     * 是否匿名发布：0=实名, 1=匿名
     */
    private Integer isAnonymous;

    private Integer isTop;

    private Integer likeCount;

    private Integer replyCount;

    private Integer status;
}

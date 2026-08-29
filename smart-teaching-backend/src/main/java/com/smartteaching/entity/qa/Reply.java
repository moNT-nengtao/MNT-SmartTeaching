package com.smartteaching.entity.qa;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName Reply
 * @Description 问答回复实体类，对应qa_reply表，存储用户对提问的回复内容及互动数据
 * @Author MNT
 * @Date 2026/8/14 15:56
 **/
@Data
@TableName("qa_reply")
public class Reply extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private String content;

    private Integer likeCount;

    private Integer status;
}

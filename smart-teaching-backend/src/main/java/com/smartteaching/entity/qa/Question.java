package com.smartteaching.entity.qa;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("qa_question")
public class Question extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String tags;

    private Integer isTop;

    private Integer likeCount;

    private Integer replyCount;

    private Integer status;
}

package com.smartteaching.entity.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Long publisherId;

    private LocalDateTime publishTime;

    private Integer isTop;

    private Integer status;
}

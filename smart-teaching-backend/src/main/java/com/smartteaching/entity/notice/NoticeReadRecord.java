package com.smartteaching.entity.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice_read_record")
public class NoticeReadRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noticeId;

    private Long userId;

    private LocalDateTime readTime;
}

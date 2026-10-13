package com.smartteaching.entity.notice;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName NoticeReadRecord
 * @Description 公告已读记录实体类，对应notice_read_record表，记录用户对公告的阅读状态
 *              注意：该表无 create_time/update_time 列，故不继承 BaseEntity
 * @Author MNT
 * @Date 2026/8/14 08:19
 **/
@Data
@TableName("notice_read_record")
public class NoticeReadRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noticeId;

    private Long userId;

    private LocalDateTime readTime;
}

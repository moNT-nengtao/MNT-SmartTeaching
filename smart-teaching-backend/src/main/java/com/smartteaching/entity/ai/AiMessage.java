package com.smartteaching.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName AiMessage
 * @Description AI消息实体类，对应ai_message表，存储对话消息记录
 * @Author MNT
 * @Date 2026/8/14 09:08
 **/
@Data
@TableName("ai_message")
public class AiMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Integer sender;

    private String content;

    private LocalDateTime createTime;
}

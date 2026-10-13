package com.smartteaching.common.dto.ai;

import lombok.Data;

/**
 * @ClassName ChatRequest
 * @Description
 * @Author MNT
 * @Date 2026/9/2 22:10
 **/
@Data
public class ChatRequest {
    private Long sessionId;      // 会话ID，为空则创建新会话
    private String message;      // 用户消息
    private String mode;         // student/teacher/analysis
    private Long studentId;      // 学业分析时使用
    private String description;  // 作业评语时使用
    private String question;     // 兼容旧前端：知识点答疑参数（与message等价）
}
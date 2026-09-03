package com.smartteaching.common.dto.ai;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName ChatResponse
 * @Description
 * @Author MNT
 * @Date 2026/9/2 22:10
 **/
@Data
@Builder
public class ChatResponse {
    private String answer;
    private Long sessionId;
    private Integer remainingCount;
    private Boolean isStream;
}
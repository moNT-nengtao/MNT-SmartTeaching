package com.smartteaching.service.ai;

import com.smartteaching.common.dto.ai.ChatRequest;
import com.smartteaching.common.dto.ai.ChatResponse;
import com.smartteaching.entity.ai.AiMessage;
import com.smartteaching.entity.ai.AiSession;

import java.util.List;

/**
 * @ClassName AiService
 * @Description AI助教业务接口
 * @Author MNT
 * @Date 2026/9/2 21:40
 **/
public interface AiService {
    ChatResponse answerQuestion(ChatRequest request, Long userId);

    ChatResponse generateComment(ChatRequest request, Long userId);

    ChatResponse analyze(ChatRequest request, Long userId);

    ChatResponse handleChat(ChatRequest request, Long userId, String mode);

    Long prepareStreamChat(ChatRequest request, Long userId);

    List<AiSession> getSessions(Long userId);

    List<AiMessage> getSessionMessages(Long userId, Long sessionId);

    void deleteSession(Long userId, Long sessionId);

    int getRemainingCount(Long userId);

    void checkDailyLimit(Long userId);

    void consumeDailyCount(Long userId);
}

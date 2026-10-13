package com.smartteaching.service.ai;

import com.smartteaching.entity.ai.AiMessage;
import com.smartteaching.entity.ai.AiSession;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @ClassName OllamaService
 * @Description
 * @Author MNT
 * @Date 2026/9/2 22:13
 **/
public interface OllamaService {
    AiSession createSession(Long userId, String title, String mode);

    List<AiSession> getSessionsByUserId(Long userId);

    List<AiMessage> getSessionHistory(Long sessionId);

    String chat(Long sessionId, String userMessage);

    Flux<String> chatStream(Long sessionId, String userMessage);

    void chatStreamWithCallback(Long sessionId, String userMessage,
                                Consumer<String> onNext,
                                Runnable onComplete);

    Map<String, Object> buildRequest(String userMessage, List<AiMessage> history, boolean stream);

    String callOllama(Map<String, Object> request) throws Exception;

    /**
     * 测试 Ollama 服务连接，返回可用模型概况
     */
    String testConnection();

    String getSystemPrompt();

    void saveUserMessage(Long sessionId, String content);

    void saveAiMessage(Long sessionId, String content);
}

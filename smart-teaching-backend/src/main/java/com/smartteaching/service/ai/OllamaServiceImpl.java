package com.smartteaching.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartteaching.config.OllamaConfig;
import com.smartteaching.entity.ai.AiMessage;
import com.smartteaching.entity.ai.AiSession;
import com.smartteaching.mapper.AiMessageMapper;
import com.smartteaching.mapper.AiSessionMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @ClassName OllamaServiceImpl
 * @Description
 * @Author MNT
 * @Date 2026/9/2 22:13
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements OllamaService {
    @Resource
    private OllamaConfig ollamaConfig;
    @Resource
    private AiSessionMapper aiSessionMapper;
    @Resource
    private AiMessageMapper aiMessageMapper;
    @Resource
    private WebClient webClient;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 创建新会话
     */
    @Override
    public AiSession createSession(Long userId, String title, String mode) {
        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : getDefaultTitle(mode));
        session.setModelName(ollamaConfig.getModel());
        LocalDateTime now = LocalDateTime.now();
        session.setCreateTime(now);
        session.setUpdateTime(now);
        aiSessionMapper.insert(session);
        return session;
    }

    private String getDefaultTitle(String mode) {
        Map<String, String> titles = new HashMap<>();
        titles.put("student", "知识点答疑");
        titles.put("teacher", "作业评语生成");
        titles.put("analysis", "学业分析");
        return titles.getOrDefault(mode, "AI对话");
    }

    /**
     * 获取用户会话列表
     */
    @Override
    public List<AiSession> getSessionsByUserId(Long userId) {
        return aiSessionMapper.selectByUserId(userId);
    }

    /**
     * 获取会话历史消息
     */
    @Override
    public List<AiMessage> getSessionHistory(Long sessionId) {
        return aiMessageMapper.selectBySessionIdOrderByCreateTime(sessionId);
    }

    /**
     * 非流式对话
     */
    @Override
    public String chat(Long sessionId, String userMessage) {
        try {
            // 1. 保存用户消息
            saveUserMessage(sessionId, userMessage);

            // 2. 获取历史消息
            List<AiMessage> history = getSessionHistory(sessionId);

            // 3. 构建请求
            Map<String, Object> request = buildRequest(userMessage, history, false);

            // 4. 调用 Ollama API
            String response = callOllama(request);

            // 5. 保存 AI 回复
            saveAiMessage(sessionId, response);

            return response;
        } catch (Exception e) {
            log.error("Ollama 调用失败", e);
            return "AI 服务暂时不可用，请稍后重试。";
        }
    }

    /**
     * 流式对话 - 返回 Flux
     */
    @Override
    public Flux<String> chatStream(Long sessionId, String userMessage) {
        try {
            saveUserMessage(sessionId, userMessage);
            List<AiMessage> history = getSessionHistory(sessionId);
            Map<String, Object> request = buildRequest(userMessage, history, true);

            StringBuilder fullResponse = new StringBuilder();
            ThinkingFilter filter = new ThinkingFilter();

            return webClient.post()
                    .uri(ollamaConfig.getResolvedHost() + "/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .map(chunk -> {
                        try {
                            JsonNode node = objectMapper.readTree(chunk);
                            if (node.has("message") && node.path("message").has("content")) {
                                String content = node.path("message").path("content").asText();
                                // 过滤 thinking 推理内容，只保留最终回答
                                String filtered = filter.next(content);
                                if (!filtered.isEmpty()) {
                                    fullResponse.append(filtered);
                                }
                                return filtered;
                            }
                            if (node.has("done") && node.path("done").asBoolean()) {
                                // 兜底：冲刷残留（无 response 标记的普通回答）
                                String rest = filter.flush();
                                if (!rest.isEmpty()) {
                                    fullResponse.append(rest);
                                    return rest;
                                }
                                saveAiMessage(sessionId, cleanResponse(fullResponse.toString()));
                                return "[DONE]";
                            }
                            return "";
                        } catch (Exception e) {
                            log.error("解析流式响应失败", e);
                            return "";
                        }
                    })
                    .filter(content -> !content.isEmpty());

        } catch (Exception e) {
            log.error("流式对话失败", e);
            return Flux.just("AI 服务暂时不可用，请稍后重试。", "[DONE]");
        }
    }

    /**
     * 流式对话 - 使用回调
     */
    @Override
    public void chatStreamWithCallback(Long sessionId, String userMessage,
                                       Consumer<String> onNext,
                                       Runnable onComplete) {
        try {
            saveUserMessage(sessionId, userMessage);
            List<AiMessage> history = getSessionHistory(sessionId);
            Map<String, Object> request = buildRequest(userMessage, history, true);

            StringBuilder fullResponse = new StringBuilder();
            ThinkingFilter filter = new ThinkingFilter();

            webClient.post()
                    .uri(ollamaConfig.getResolvedHost() + "/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .subscribe(
                            chunk -> {
                                try {
                                    JsonNode node = objectMapper.readTree(chunk);
                                    if (node.has("message") && node.path("message").has("content")) {
                                        String content = node.path("message").path("content").asText();
                                        // 过滤 thinking 推理内容，只保留最终回答
                                        String filtered = filter.next(content);
                                        if (!filtered.isEmpty()) {
                                            fullResponse.append(filtered);
                                            onNext.accept(filtered);
                                        }
                                    }
                                    if (node.has("done") && node.path("done").asBoolean()) {
                                        // 兜底：冲刷残留（无 response 标记的普通回答）
                                        String rest = filter.flush();
                                        if (!rest.isEmpty()) {
                                            fullResponse.append(rest);
                                            onNext.accept(rest);
                                        }
                                        saveAiMessage(sessionId, cleanResponse(fullResponse.toString()));
                                        if (onComplete != null) {
                                            onComplete.run();
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("解析流式响应失败", e);
                                }
                            },
                            error -> {
                                log.error("流式对话出错", error);
                                onNext.accept("AI 服务出错，请稍后重试。");
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                    );

        } catch (Exception e) {
            log.error("流式对话失败", e);
            onNext.accept("AI 服务暂时不可用");
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * 构建 Ollama 请求
     */
    @Override
    public Map<String, Object> buildRequest(String userMessage, List<AiMessage> history, boolean stream) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", getSystemPrompt());
        messages.add(systemMsg);

        // 添加历史消息
        int maxHistory = ollamaConfig.getMaxHistory();
        int start = Math.max(0, history.size() - maxHistory);
        for (int i = start; i < history.size(); i++) {
            AiMessage msg = history.get(i);
            Map<String, String> m = new HashMap<>();
            m.put("role", msg.getSender() == 0 ? "user" : "assistant");
            m.put("content", msg.getContent());
            messages.add(m);
        }

        // 当前用户消息
        Map<String, String> currentMsg = new HashMap<>();
        currentMsg.put("role", "user");
        currentMsg.put("content", userMessage);
        messages.add(currentMsg);

        Map<String, Object> request = new HashMap<>();
        request.put("model", ollamaConfig.getModel());
        request.put("messages", messages);
        request.put("stream", stream);
        request.put("options", Map.of(
                "temperature", 0.7,
                "top_p", 0.9
        ));

        return request;
    }

    /**
     * 调用 Ollama API（非流式）
     */
    @Override
    public String callOllama(Map<String, Object> request) throws Exception {
        String response = webClient.post()
                .uri(ollamaConfig.getResolvedHost() + "/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(ollamaConfig.getTimeout()))
                .block();

        if (response == null) {
            return "AI 服务无响应";
        }

        JsonNode jsonNode = objectMapper.readTree(response);
        if (jsonNode.has("error")) {
            return "AI 服务错误: " + jsonNode.get("error").asText();
        }

        JsonNode message = jsonNode.path("message");
        return cleanResponse(message.path("content").asText());
    }

    /**
     * 清洗 qwen 系列推理模型的输出：去掉 think 推理块，只保留最终回答
     */
    private String cleanResponse(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String cleaned = raw.replaceAll("(?s)<think>.*?</think>", "");
        return cleaned.trim();
    }

    /**
     * 流式输出过滤状态机：去掉 qwen 系推理模型的 &lt;think&gt;...&lt;/think&gt; 推理块，
     * 只透出最终回答。通过保留尾部缓冲应对标记跨 chunk 切分的情况。
     */
    private static class ThinkingFilter {
        private static final String OPEN = "<think>";
        private static final String CLOSE = "</think>";
        private final StringBuilder buf = new StringBuilder();
        private boolean inThinking = false;

        String next(String chunk) {
            if (chunk == null) {
                return "";
            }
            buf.append(chunk);
            StringBuilder out = new StringBuilder();
            // 循环识别完整标记
            boolean progress = true;
            while (progress) {
                progress = false;
                if (!inThinking) {
                    int idx = buf.indexOf(OPEN);
                    if (idx >= 0) {
                        out.append(buf.substring(0, idx));
                        buf.delete(0, idx + OPEN.length());
                        inThinking = true;
                        progress = true;
                    }
                } else {
                    int idx = buf.indexOf(CLOSE);
                    if (idx >= 0) {
                        buf.delete(0, idx + CLOSE.length());
                        inThinking = false;
                        progress = true;
                    }
                }
            }
            // 输出安全前缀（保留尾部缓冲，避免标记被跨 chunk 切分）
            int keep = inThinking ? CLOSE.length() - 1 : OPEN.length() - 1;
            int safeLen = buf.length() - keep;
            if (safeLen > 0) {
                if (!inThinking) {
                    out.append(buf.substring(0, safeLen));
                }
                buf.delete(0, safeLen);
            }
            return out.toString();
        }

        /** 连接结束时刷出残留内容（兜底） */
        String flush() {
            String s = buf.toString();
            buf.setLength(0);
            return s;
        }
    }

    /**
     * 测试 Ollama 服务连接，返回可用模型概况
     */
    @Override
    public String testConnection() {
        try {
            String body = webClient.get()
                    .uri(ollamaConfig.getResolvedHost() + "/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .block();
            if (body == null || body.isBlank()) {
                return "Ollama 服务响应为空";
            }
            JsonNode root = objectMapper.readTree(body);
            JsonNode models = root.path("models");
            int count = models.isArray() ? models.size() : 0;
            return "Ollama 服务正常，共 " + count + " 个模型可用";
        } catch (Exception e) {
            log.error("测试 Ollama 连接失败", e);
            throw new RuntimeException("连接 Ollama 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 系统提示词
     */
    @Override
    public String getSystemPrompt() {
        return """
                你是一个智能教学助教，名叫"小智"。你的职责是：
                1. 帮助学生解答课程相关的知识点问题
                2. 辅助教师生成作业评语
                3. 提供学业分析和建议
                请用友好、专业、耐心的语气回答问题。
                回答要简洁明了，适当举例说明。
                """;
    }

    /**
     * 保存用户消息
     */
    @Override
    public void saveUserMessage(Long sessionId, String content) {
        AiMessage msg = new AiMessage();
        msg.setSessionId(sessionId);
        msg.setSender(0);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(msg);
    }

    /**
     * 保存 AI 消息
     */
    @Override
    public void saveAiMessage(Long sessionId, String content) {
        AiMessage msg = new AiMessage();
        msg.setSessionId(sessionId);
        msg.setSender(1);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        aiMessageMapper.insert(msg);
    }
}

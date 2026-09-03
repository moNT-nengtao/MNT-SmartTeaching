package com.smartteaching.controller.ai;

import com.smartteaching.common.dto.ai.ChatRequest;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.entity.ai.AiSession;
import com.smartteaching.mapper.AiSessionMapper;
import com.smartteaching.service.ai.AiService;
import com.smartteaching.service.ai.OllamaService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName OllamaController
 * @Description Ollama 流式对话控制器（SSE）
 * @Author MNT
 * @Date 2026/9/2 22:20
 **/
@Slf4j
@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaController {
    @Resource
    private OllamaService ollamaService;
    @Resource
    private AiService aiService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private AiSessionMapper aiSessionMapper;
    /** SSE 流式对话线程池（自行创建，避免容器中无 ExecutorService Bean 导致启动失败） */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * SSE 流式对话
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestParam Long sessionId,
            @RequestParam String message,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        // 校验会话归属
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BaseException("无权操作该会话");
        }
        // 每日问答次数限制：先校验再扣减（防刷限流）
        aiService.checkDailyLimit(userId);
        aiService.consumeDailyCount(userId);

        SseEmitter emitter = new SseEmitter(120000L);

        executor.execute(() -> {
            try {
                // 发送连接成功事件
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("连接成功"));

                // 开始流式对话
                ollamaService.chatStreamWithCallback(
                        sessionId,
                        message,
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                            } catch (IOException e) {
                                log.error("发送消息失败", e);
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("[DONE]"));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("发送完成事件失败", e);
                                emitter.completeWithError(e);
                            }
                        }
                );

            } catch (Exception e) {
                log.error("SSE 流式对话失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 服务暂时不可用"));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        // 超时处理
        emitter.onTimeout(() -> {
            log.warn("SSE 超时，sessionId: {}", sessionId);
            emitter.complete();
        });

        // 异常处理
        emitter.onError((e) -> {
            log.error("SSE 异常", e);
            emitter.complete();
        });

        return emitter;
    }

    /**
     * 创建会话并流式对话（一步到位）
     * 相比 streamChat 额外推送一个 session 事件，携带新建的会话ID，供前端记录会话
     */
    @GetMapping(value = "/chat/stream/new", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter newStreamChat(
            @RequestParam String message,
            @RequestParam String mode,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(message);
        chatRequest.setMode(mode);

        // 创建/校验会话（不在此扣减，由下方统一处理次数限制）
        Long sessionId = aiService.prepareStreamChat(chatRequest, userId);

        // 每日问答次数限制：先校验再扣减（防刷限流）
        aiService.checkDailyLimit(userId);
        aiService.consumeDailyCount(userId);

        SseEmitter emitter = new SseEmitter(120000L);
        executor.execute(() -> {
            try {
                // 先推送会话ID，便于前端记录新会话
                emitter.send(SseEmitter.event()
                        .name("session")
                        .data("{\"sessionId\":" + sessionId + "}"));
                // 发送连接成功事件
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("连接成功"));
                // 开始流式对话
                ollamaService.chatStreamWithCallback(
                        sessionId,
                        message,
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                            } catch (IOException e) {
                                log.error("发送消息失败", e);
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("[DONE]"));
                                emitter.complete();
                            } catch (IOException e) {
                                log.error("发送完成事件失败", e);
                                emitter.completeWithError(e);
                            }
                        }
                );
            } catch (Exception e) {
                log.error("SSE 流式对话失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 服务暂时不可用"));
                } catch (IOException ex) {
                    // ignore
                }
                emitter.completeWithError(e);
            }
        });

        // 超时处理
        emitter.onTimeout(() -> {
            log.warn("SSE 超时，sessionId: {}", sessionId);
            emitter.complete();
        });

        // 异常处理
        emitter.onError((e) -> {
            log.error("SSE 异常", e);
            emitter.complete();
        });

        return emitter;
    }

    /**
     * 测试 Ollama 连接（真实探测服务可用性与模型列表）
     */
    @GetMapping("/test")
    public Result<String> testOllama() {
        try {
            return Result.success(ollamaService.testConnection());
        } catch (Exception e) {
            log.error("测试 Ollama 连接失败", e);
            return Result.error("Ollama 服务不可用: " + e.getMessage());
        }
    }

    /**
     * 从 JWT 请求头获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        return jwtUtil.getUserIdFromHeader(authHeader);
    }
}

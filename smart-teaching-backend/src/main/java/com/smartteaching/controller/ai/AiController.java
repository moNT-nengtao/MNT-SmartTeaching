package com.smartteaching.controller.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.dto.ai.ChatRequest;
import com.smartteaching.common.dto.ai.ChatResponse;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.ai.AiStudentVO;
import com.smartteaching.entity.ai.AiMessage;
import com.smartteaching.entity.ai.AiSession;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.UserMapper;
import com.smartteaching.service.ai.AiService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName AiController
 * @Description AI助教控制器：知识点答疑、作业评语生成、学业分析、会话管理、每日次数限制
 * @Author MNT
 * @Date 2026/9/2 21:40
 **/
@RestController
@RequestMapping("/api/ai")
@Slf4j
@RequiredArgsConstructor
public class AiController {
    @Resource
    private AiService aiService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private UserMapper userMapper;

    /**
     * 知识点答疑（所有登录用户）
     */
    @PostMapping("/answer")
    public Result<ChatResponse> answer(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        request.setMode("student");
        ChatResponse response = aiService.answerQuestion(request, userId);
        return Result.success(response);
    }

    /**
     * 作业评语生成（教师/管理员）
     */
    @PostMapping("/comment")
    public Result<ChatResponse> comment(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        checkTeacherOrAdmin(userId);
        request.setMode("teacher");
        ChatResponse response = aiService.generateComment(request, userId);
        return Result.success(response);
    }

    /**
     * 学业分析（教师/管理员）
     */
    @GetMapping("/analysis/{studentId}")
    public Result<ChatResponse> analysis(@PathVariable Long studentId,
                                         @RequestParam(required = false) String message,
                                         HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        checkTeacherOrAdmin(userId);
        ChatRequest request = new ChatRequest();
        request.setStudentId(studentId);
        request.setMessage(message != null && !message.isBlank() ? message : "请分析该学生的学业情况");
        request.setMode("analysis");
        ChatResponse response = aiService.analyze(request, userId);
        return Result.success(response);
    }

    /**
     * 通用对话（自动识别模式）
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        String mode = request.getMode() == null || request.getMode().isBlank() ? "student" : request.getMode();
        // 教师/分析模式仅教师/管理员可用
        if ("teacher".equals(mode) || "analysis".equals(mode)) {
            checkTeacherOrAdmin(userId);
        }
        ChatResponse response = aiService.handleChat(request, userId, mode);
        return Result.success(response);
    }

    /**
     * 获取用户会话列表（仅本人）
     */
    @GetMapping("/sessions")
    public Result<List<AiSession>> getSessions(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return Result.success(aiService.getSessions(userId));
    }

    /**
     * 历史对话记录（兼容前端 /ai/history，返回本人会话列表）
     */
    @GetMapping("/history")
    public Result<List<AiSession>> getHistory(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return Result.success(aiService.getSessions(userId));
    }

    /**
     * 获取会话历史消息（校验归属）
     */
    @GetMapping("/session/{sessionId}/messages")
    public Result<List<AiMessage>> getMessages(@PathVariable Long sessionId, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return Result.success(aiService.getSessionMessages(userId, sessionId));
    }

    /**
     * 删除会话（校验归属）
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        aiService.deleteSession(userId, sessionId);
        return Result.success();
    }

    /**
     * 获取今日剩余次数
     */
    @GetMapping("/remaining")
    public Result<Integer> getRemainingCount(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        return Result.success(aiService.getRemainingCount(userId));
    }

    /**
     * 学生列表（教师/管理员，供学业分析选人）
     */
    @GetMapping("/students")
    public Result<List<AiStudentVO>> getStudents(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        checkTeacherOrAdmin(userId);
        List<AiStudentVO> list = userMapper.selectList(
                        Wrappers.<User>lambdaQuery()
                                .eq(User::getRole, "student")
                                .eq(User::getStatus, 1)
                                .orderByAsc(User::getId))
                .stream()
                .map(u -> {
                    AiStudentVO vo = new AiStudentVO();
                    vo.setId(u.getId());
                    vo.setUsername(u.getUsername());
                    vo.setRealName(u.getRealName());
                    return vo;
                })
                .toList();
        return Result.success(list);
    }

    /**
     * 从 JWT 请求头获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        return jwtUtil.getUserIdFromHeader(authHeader);
    }

    /**
     * 校验当前用户是否为教师/管理员
     */
    private void checkTeacherOrAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !("teacher".equals(user.getRole()) || "admin".equals(user.getRole()))) {
            throw new BaseException("无权限执行该操作");
        }
    }
}

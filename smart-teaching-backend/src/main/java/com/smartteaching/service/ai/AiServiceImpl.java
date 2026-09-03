package com.smartteaching.service.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.dto.ai.ChatRequest;
import com.smartteaching.common.dto.ai.ChatResponse;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.config.OllamaConfig;
import com.smartteaching.entity.ai.AiDailyUsage;
import com.smartteaching.entity.ai.AiMessage;
import com.smartteaching.entity.ai.AiSession;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.AiAnalysisMapper;
import com.smartteaching.mapper.AiDailyUsageMapper;
import com.smartteaching.mapper.AiMessageMapper;
import com.smartteaching.mapper.AiSessionMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AiServiceImpl
 * @Description AI助教业务实现：答疑、评语生成、学业分析、会话管理与每日次数限制
 * @Author MNT
 * @Date 2026/9/2 21:40
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
    @Resource
    private OllamaService ollamaService;
    @Resource
    private AiSessionMapper aiSessionMapper;
    @Resource
    private AiMessageMapper aiMessageMapper;
    @Resource
    private AiDailyUsageMapper aiDailyUsageMapper;
    @Resource
    private AiAnalysisMapper aiAnalysisMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private OllamaConfig ollamaConfig;

    /**
     * 知识点答疑
     */
    @Override
    public ChatResponse answerQuestion(ChatRequest request, Long userId) {
        return handleChat(request, userId, "student");
    }

    /**
     * 作业评语生成
     */
    @Override
    public ChatResponse generateComment(ChatRequest request, Long userId) {
        // 评语模式的输入放在 description 字段，统一转成 message
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            request.setMessage(request.getDescription());
        }
        return handleChat(request, userId, "teacher");
    }

    /**
     * 学业分析：拉取学生成绩、考勤、作业数据组装提示词
     */
    @Override
    public ChatResponse analyze(ChatRequest request, Long userId) {
        Long studentId = request.getStudentId();
        if (studentId == null) {
            throw new BaseException("请选择要分析的学生");
        }
        User student = userMapper.selectById(studentId);
        if (student == null || !"student".equals(student.getRole())) {
            throw new BaseException("学生不存在或不是学生角色");
        }

        String studentData = buildStudentAnalysisData(studentId);
        String enhancedMessage = String.format(
                "请基于以下学生数据，分析该学生（姓名：%s，学号：%s）的学业现状，" +
                        "指出其优势科目与薄弱环节，并给出针对性的学习提升建议：\n%s",
                student.getRealName(), student.getUsername(), studentData);
        request.setMessage(enhancedMessage);
        return handleChat(request, userId, "analysis");
    }

    /**
     * 组装学业分析所需的学生数据文本
     */
    private String buildStudentAnalysisData(Long studentId) {
        StringBuilder sb = new StringBuilder();

        Map<String, Object> score = aiAnalysisMapper.selectScoreSummary(studentId);
        if (score == null) {
            sb.append("【成绩概况】暂无成绩记录");
        } else {
            sb.append("【成绩概况】共").append(score.get("courseCount")).append("门课程，")
                    .append("平均分").append(score.get("avgScore")).append("，")
                    .append("最高分").append(score.get("maxScore")).append("，")
                    .append("最低分").append(score.get("minScore")).append("，")
                    .append("及格").append(score.get("passCount")).append("门，")
                    .append("不及格").append(score.get("failCount")).append("门");
        }

        List<Map<String, Object>> details = aiAnalysisMapper.selectScoreDetail(studentId);
        if (details != null && !details.isEmpty()) {
            sb.append("；各科成绩明细：");
            for (int i = 0; i < details.size(); i++) {
                Map<String, Object> d = details.get(i);
                sb.append(d.get("courseName")).append("(").append(d.get("semester")).append(")")
                        .append("总分").append(d.get("totalScore"))
                        .append("[平时").append(d.get("usualScore")).append("，期末").append(d.get("finalScore")).append("]");
                if (i < details.size() - 1) {
                    sb.append("，");
                }
            }
        }

        Map<String, Object> att = aiAnalysisMapper.selectAttendanceSummary(studentId);
        if (att == null) {
            sb.append("；\n【考勤概况】暂无考勤记录");
        } else {
            sb.append("；\n【考勤概况】应到").append(att.get("totalSessions")).append("次，")
                    .append("出勤").append(att.get("presentCount")).append("，")
                    .append("迟到").append(att.get("lateCount")).append("，")
                    .append("请假").append(att.get("leaveCount")).append("，")
                    .append("缺勤/旷课").append(att.get("absentCount"));
        }

        Map<String, Object> hw = aiAnalysisMapper.selectHomeworkSummary(studentId);
        if (hw == null) {
            sb.append("；\n【作业概况】暂无作业记录");
        } else {
            sb.append("；\n【作业概况】已提交作业").append(hw.get("submittedCount")).append("份，")
                    .append("已批改").append(hw.get("gradedCount")).append("份，")
                    .append("作业平均分").append(hw.get("avgHomeworkScore"));
        }

        return sb.toString();
    }

    /**
     * 通用对话处理（非流式）
     */
    @Override
    public ChatResponse handleChat(ChatRequest request, Long userId, String mode) {
        // 每日问答次数限制：先校验再扣减（防刷限流）
        checkAndConsume(userId);

        Long sessionId = resolveSession(request, userId, mode);

        String userMessage = normalizeMessage(request);
        String answer = ollamaService.chat(sessionId, userMessage);

        return ChatResponse.builder()
                .answer(answer)
                .sessionId(sessionId)
                .isStream(false)
                .remainingCount(getRemainingCount(userId))
                .build();
    }

    /**
     * 流式对话：仅创建/校验会话，次数扣减在流式接口中统一处理
     */
    @Override
    public Long prepareStreamChat(ChatRequest request, Long userId) {
        return resolveSession(request, userId, request.getMode());
    }

    /**
     * 解析并校验会话：为空则新建，否则校验归属
     */
    private Long resolveSession(ChatRequest request, Long userId, String mode) {
        Long sessionId = request.getSessionId();
        if (sessionId == null || sessionId <= 0) {
            AiSession session = ollamaService.createSession(userId, null, mode);
            return session.getId();
        }
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BaseException("无权操作该会话");
        }
        return sessionId;
    }

    /**
     * 归一化消息内容：兼容 message / question / description 三种入参
     */
    private String normalizeMessage(ChatRequest request) {
        String msg = request.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = request.getQuestion();
        }
        if (msg == null || msg.isBlank()) {
            msg = request.getDescription();
        }
        if (msg == null || msg.isBlank()) {
            throw new BaseException("消息内容不能为空");
        }
        return msg.trim();
    }

    /**
     * 获取用户会话列表（仅本人）
     */
    @Override
    public List<AiSession> getSessions(Long userId) {
        return ollamaService.getSessionsByUserId(userId);
    }

    /**
     * 获取会话历史消息（校验归属）
     */
    @Override
    public List<AiMessage> getSessionMessages(Long userId, Long sessionId) {
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BaseException("无权查看该会话");
        }
        return ollamaService.getSessionHistory(sessionId);
    }

    /**
     * 删除会话及其消息（校验归属）
     */
    @Override
    public void deleteSession(Long userId, Long sessionId) {
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BaseException("无权删除该会话");
        }
        aiMessageMapper.delete(Wrappers.<AiMessage>lambdaQuery().eq(AiMessage::getSessionId, sessionId));
        aiSessionMapper.deleteById(sessionId);
        log.info("用户{}删除会话{}成功", userId, sessionId);
    }

    /**
     * 查询今日剩余次数
     */
    @Override
    public int getRemainingCount(Long userId) {
        AiDailyUsage usage = aiDailyUsageMapper.selectByUserAndDate(userId, LocalDate.now());
        int used = usage == null ? 0 : usage.getUseCount();
        int limit = ollamaConfig.getDailyLimit() == null ? 30 : ollamaConfig.getDailyLimit();
        return Math.max(0, limit - used);
    }

    /**
     * 校验每日次数是否充足，不足抛业务异常
     */
    @Override
    public void checkDailyLimit(Long userId) {
        if (getRemainingCount(userId) <= 0) {
            throw new BaseException("今日AI问答次数已用完，请明天再试");
        }
    }

    /**
     * 今日使用次数 +1
     */
    @Override
    public void consumeDailyCount(Long userId) {
        aiDailyUsageMapper.incrementUsage(userId, LocalDate.now());
    }

    /**
     * 每日次数校验 + 扣减（非流式对话与流式对话共用）
     */
    private void checkAndConsume(Long userId) {
        checkDailyLimit(userId);
        consumeDailyCount(userId);
    }
}

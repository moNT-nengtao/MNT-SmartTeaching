package com.smartteaching.service.qa;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteaching.common.dto.qa.QaQueryDTO;
import com.smartteaching.common.dto.qa.QaQuestionSaveDTO;
import com.smartteaching.common.dto.qa.QaReplyDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.SecurityUtils;
import com.smartteaching.common.vo.qa.QaDetailVO;
import com.smartteaching.common.vo.qa.QaQuestionListVO;
import com.smartteaching.common.vo.qa.QaReplyVO;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.qa.Question;
import com.smartteaching.entity.qa.Reply;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.QaMapper;
import com.smartteaching.mapper.QaReplyMapper;
import com.smartteaching.mapper.SelectionMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName QaServiceImpl
 * @Description 答疑社区 Service 实现
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Service
@Slf4j
public class QaServiceImpl implements QaService {

    @Resource
    private QaMapper qaMapper;

    @Resource
    private QaReplyMapper replyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SelectionMapper selectionMapper;

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new BaseException("未登录，请先登录");
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            throw new BaseException("当前用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BaseException("账号已被禁用，无法操作");
        }
        return user;
    }

    /**
     * 分页查询问题列表
     */
    @Override
    public PageResult<QaQuestionListVO> getQuestionList(QaQueryDTO dto) {
        getCurrentUser();
        long pageNum = dto.getPage() == null ? 1 : dto.getPage();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        IPage<QaQuestionListVO> iPage = new Page<>(pageNum, pageSize);
        IPage<QaQuestionListVO> voIPage = qaMapper.selectQuestionPage(iPage, dto);
        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 问题详情
     */
    @Override
    public QaDetailVO getQuestionDetail(Long id) {
        getCurrentUser();
        QaQuestionListVO question = qaMapper.selectQuestionDetail(id);
        if (question == null) {
            throw new BaseException("问题不存在或已删除");
        }
        List<QaReplyVO> replies = qaMapper.selectReplies(id);
        QaDetailVO detailVO = new QaDetailVO();
        detailVO.setQuestion(question);
        detailVO.setReplies(replies);
        return detailVO;
    }

    /**
     * 发布问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishQuestion(QaQuestionSaveDTO dto) {
        User currentUser = getCurrentUser();
        if (dto.getCourseId() == null) {
            throw new BaseException("请选择课程");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BaseException("请输入问题标题");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BaseException("请输入问题内容");
        }
        if (dto.getTag() == null || dto.getTag().trim().isEmpty()) {
            throw new BaseException("请选择问题标签");
        }

        // 校验课程存在且启用
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BaseException("所选课程不存在");
        }
        if (course.getStatus() != null && course.getStatus() == 0) {
            throw new BaseException("所选课程已被禁用");
        }

        // 权限校验：学生只能在自己选修的课程分区提问，教师只能在自己授课的课程分区提问，管理员不限
        if ("student".equals(currentUser.getRole())) {
            boolean selected = selectionMapper.hasSelected(currentUser.getId(), course.getId());
            if (!selected) {
                throw new BaseException("请先选修该课程后再提问");
            }
        } else if ("teacher".equals(currentUser.getRole())) {
            if (course.getTeacherId() == null || !course.getTeacherId().equals(currentUser.getId())) {
                throw new BaseException("只能在自己授课的课程分区提问");
            }
        }

        Question question = new Question();
        question.setUserId(currentUser.getId());
        question.setCourseId(course.getId());
        question.setTitle(dto.getTitle().trim());
        question.setContent(dto.getContent());
        question.setTags(dto.getTag().trim());
        question.setIsAnonymous(dto.getIsAnonymous() != null && dto.getIsAnonymous() ? 1 : 0);
        question.setIsTop(0);
        question.setLikeCount(0);
        question.setReplyCount(0);
        question.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        question.setCreateTime(now);
        question.setUpdateTime(now);
        qaMapper.insert(question);
        log.info("用户[{}]在课程[{}]发布问题[{}]成功", currentUser.getRealName(), course.getName(), question.getTitle());
    }

    /**
     * 回复问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyQuestion(QaReplyDTO dto) {
        User currentUser = getCurrentUser();
        if (dto.getQuestionId() == null) {
            throw new BaseException("问题ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BaseException("请输入回复内容");
        }
        Question question = qaMapper.selectById(dto.getQuestionId());
        if (question == null || question.getStatus() == 0) {
            throw new BaseException("问题不存在或已删除");
        }

        Reply reply = new Reply();
        reply.setQuestionId(question.getId());
        reply.setUserId(currentUser.getId());
        reply.setContent(dto.getContent().trim());
        reply.setLikeCount(0);
        reply.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        reply.setCreateTime(now);
        reply.setUpdateTime(now);
        replyMapper.insert(reply);

        // 回复数 +1
        qaMapper.update(null, Wrappers.<Question>lambdaUpdate()
                .eq(Question::getId, question.getId())
                .setSql("reply_count = reply_count + 1"));
        log.info("用户[{}]回复问题[{}]成功", currentUser.getRealName(), question.getId());
    }

    /**
     * 点赞问题
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeQuestion(Long id) {
        getCurrentUser();
        int updated = qaMapper.update(null, Wrappers.<Question>lambdaUpdate()
                .eq(Question::getId, id)
                .eq(Question::getStatus, 1)
                .setSql("like_count = like_count + 1"));
        if (updated == 0) {
            throw new BaseException("问题不存在或已删除");
        }
        log.info("问题[{}]点赞成功", id);
    }

    /**
     * 点赞回复
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeReply(Long replyId) {
        getCurrentUser();
        int updated = replyMapper.update(null, Wrappers.<Reply>lambdaUpdate()
                .eq(Reply::getId, replyId)
                .eq(Reply::getStatus, 1)
                .setSql("like_count = like_count + 1"));
        if (updated == 0) {
            throw new BaseException("回复不存在或已删除");
        }
        log.info("回复[{}]点赞成功", replyId);
    }

    /**
     * 置顶/取消置顶问题（仅管理员/教师）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long id, Integer isTop) {
        User currentUser = getCurrentUser();
        if (!"admin".equals(currentUser.getRole()) && !"teacher".equals(currentUser.getRole())) {
            throw new BaseException("只有管理员或教师可以置顶问题");
        }
        Question question = qaMapper.selectById(id);
        if (question == null || question.getStatus() == 0) {
            throw new BaseException("问题不存在或已删除");
        }
        question.setIsTop(isTop != null && isTop == 1 ? 1 : 0);
        question.setUpdateTime(LocalDateTime.now());
        qaMapper.updateById(question);
        log.info("问题[{}]置顶状态更新为[{}]", id, question.getIsTop());
    }

    /**
     * 获取全部去重标签
     */
    @Override
    public List<String> getTags() {
        getCurrentUser();
        List<String> rawList = qaMapper.selectAllTags();
        Set<String> tagSet = new LinkedHashSet<>();
        for (String raw : rawList) {
            if (raw == null || raw.trim().isEmpty()) {
                continue;
            }
            for (String tag : raw.split(",")) {
                if (tag != null && !tag.trim().isEmpty()) {
                    tagSet.add(tag.trim());
                }
            }
        }
        return new ArrayList<>(tagSet);
    }
}

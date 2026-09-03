package com.smartteaching.service.homework;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.dto.homework.HomeworkGradeDTO;
import com.smartteaching.common.dto.homework.HomeworkQueryDTO;
import com.smartteaching.common.dto.homework.HomeworkSaveDTO;
import com.smartteaching.common.dto.homework.HomeworkSubmitDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.SecurityUtils;
import com.smartteaching.common.vo.homework.HomeworkListVO;
import com.smartteaching.common.vo.homework.HomeworkStatsVO;
import com.smartteaching.common.vo.homework.HomeworkStudentListVO;
import com.smartteaching.common.vo.homework.HomeworkSubmissionVO;
import com.smartteaching.entity.homework.Homework;
import com.smartteaching.entity.homework.HomeworkSubmission;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.HomeworkMapper;
import com.smartteaching.mapper.HomeworkSubmissionMapper;
import com.smartteaching.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @ClassName HomeworkServiceImpl
 * @Description 作业 Service 实现
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Service
@Slf4j
public class HomeworkServiceImpl implements HomeworkService {

    @Resource
    private HomeworkMapper homeworkMapper;

    @Resource
    private HomeworkSubmissionMapper submissionMapper;

    @Resource
    private UserMapper userMapper;

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
        return user;
    }

    /**
     * 保存上传附件到 uploads/homework/，返回访问 URL
     */
    private String saveAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        String projectBase = System.getProperty("user.dir");
        File baseDir = new File(projectBase, "uploads/homework");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        File destFile = new File(baseDir, fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("作业附件保存失败", e);
            throw new BaseException("附件保存失败，请重试");
        }
        return "/files/homework/" + fileName;
    }

    /**
     * 发布作业
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishHomework(HomeworkSaveDTO dto, MultipartFile file) {
        User currentUser = getCurrentUser();
        if (!"teacher".equals(currentUser.getRole())) {
            throw new BaseException("只有教师可以发布作业");
        }
        if (dto.getCourseId() == null) {
            throw new BaseException("请选择课程");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BaseException("请输入作业标题");
        }

        Homework homework = new Homework();
        homework.setCourseId(dto.getCourseId());
        homework.setTeacherId(currentUser.getId());
        homework.setTitle(dto.getTitle().trim());
        homework.setContent(dto.getContent());
        homework.setDeadline(dto.getDeadline());
        homework.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        homework.setCreateTime(now);
        homework.setUpdateTime(now);

        // 保存附件
        if (file != null && !file.isEmpty()) {
            String fileUrl = saveAttachment(file);
            homework.setAttachmentUrl(fileUrl);
            homework.setAttachmentName(file.getOriginalFilename());
        }

        homeworkMapper.insert(homework);
        log.info("教师[{}]发布作业[{}]成功", currentUser.getRealName(), homework.getTitle());
    }

    /**
     * 编辑作业
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHomework(HomeworkSaveDTO dto, MultipartFile file) {
        User currentUser = getCurrentUser();
        if (dto.getId() == null) {
            throw new BaseException("作业ID不能为空");
        }
        Homework homework = homeworkMapper.selectById(dto.getId());
        if (homework == null || homework.getStatus() == 0) {
            throw new BaseException("作业不存在或已删除");
        }
        // 权限校验：只有发布教师本人可以编辑
        if (!homework.getTeacherId().equals(currentUser.getId())
                && !"admin".equals(currentUser.getRole())) {
            throw new BaseException("无权编辑该作业");
        }

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            homework.setTitle(dto.getTitle().trim());
        }
        if (dto.getContent() != null) {
            homework.setContent(dto.getContent());
        }
        if (dto.getDeadline() != null) {
            homework.setDeadline(dto.getDeadline());
        }
        if (dto.getCourseId() != null) {
            homework.setCourseId(dto.getCourseId());
        }
        homework.setUpdateTime(LocalDateTime.now());

        // 如果上传了新附件，替换旧附件
        if (file != null && !file.isEmpty()) {
            String fileUrl = saveAttachment(file);
            homework.setAttachmentUrl(fileUrl);
            homework.setAttachmentName(file.getOriginalFilename());
        }

        homeworkMapper.updateById(homework);
        log.info("作业[{}]编辑成功", dto.getId());
    }

    /**
     * 删除作业（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHomework(Long id) {
        User currentUser = getCurrentUser();
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null || homework.getStatus() == 0) {
            throw new BaseException("作业不存在或已删除");
        }
        if (!homework.getTeacherId().equals(currentUser.getId())
                && !"admin".equals(currentUser.getRole())) {
            throw new BaseException("无权删除该作业");
        }
        homework.setStatus(0);
        homework.setUpdateTime(LocalDateTime.now());
        homeworkMapper.updateById(homework);
        log.info("作业[{}]删除成功", id);
    }

    /**
     * 教师/管理员 作业列表
     */
    @Override
    public PageResult<HomeworkListVO> getHomeworkList(HomeworkQueryDTO queryDTO) {
        User currentUser = getCurrentUser();
        long pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

        // 教师只能看自己发布的作业，管理员看全部
        if ("teacher".equals(currentUser.getRole())) {
            queryDTO.setTeacherId(currentUser.getId());
        } else if (!"admin".equals(currentUser.getRole())) {
            throw new BaseException("无权访问作业管理列表");
        }

        IPage<HomeworkListVO> iPage = new Page<>(pageNum, pageSize);
        IPage<HomeworkListVO> voIPage = homeworkMapper.selectHomeworkPage(iPage, queryDTO);

        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 学生 作业列表（含提交状态和成绩）
     */
    @Override
    public PageResult<HomeworkStudentListVO> getStudentHomeworkList(HomeworkQueryDTO queryDTO) {
        User currentUser = getCurrentUser();
        if (!"student".equals(currentUser.getRole())) {
            throw new BaseException("只有学生可以访问我的作业列表");
        }
        long pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        long pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

        IPage<HomeworkStudentListVO> iPage = new Page<>(pageNum, pageSize);
        IPage<HomeworkStudentListVO> voIPage = homeworkMapper.selectStudentHomeworkPage(
                iPage, queryDTO, currentUser.getId());

        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 作业详情
     */
    @Override
    public Homework getHomeworkDetail(Long id) {
        User currentUser = getCurrentUser();
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null || homework.getStatus() == 0) {
            throw new BaseException("作业不存在或已删除");
        }
        // 学生只能看自己选课的作业
        if ("student".equals(currentUser.getRole())) {
            Long count = homeworkMapper.countStudentCourse(currentUser.getId(), homework.getCourseId());
            if (count == null || count == 0) {
                throw new BaseException("无权查看该作业");
            }
        }
        return homework;
    }

    /**
     * 查看某作业的提交列表
     */
    @Override
    public List<HomeworkSubmissionVO> getSubmissionList(Long homeworkId) {
        User currentUser = getCurrentUser();
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null || homework.getStatus() == 0) {
            throw new BaseException("作业不存在或已删除");
        }
        if (!homework.getTeacherId().equals(currentUser.getId())
                && !"admin".equals(currentUser.getRole())) {
            throw new BaseException("无权查看该作业的提交列表");
        }
        return homeworkMapper.selectSubmissionList(homeworkId);
    }

    /**
     * 学生提交作业（已提交则覆盖更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitHomework(HomeworkSubmitDTO dto, MultipartFile file) {
        User currentUser = getCurrentUser();
        if (!"student".equals(currentUser.getRole())) {
            throw new BaseException("只有学生可以提交作业");
        }
        if (dto.getHomeworkId() == null) {
            throw new BaseException("作业ID不能为空");
        }
        Homework homework = homeworkMapper.selectById(dto.getHomeworkId());
        if (homework == null || homework.getStatus() == 0) {
            throw new BaseException("作业不存在或已删除");
        }
        // 校验学生是否选了该课程
        Long count = homeworkMapper.countStudentCourse(currentUser.getId(), homework.getCourseId());
        if (count == null || count == 0) {
            throw new BaseException("您未选修该课程，无法提交作业");
        }
        // 截止时间校验
        if (homework.getDeadline() != null && LocalDateTime.now().isAfter(homework.getDeadline())) {
            throw new BaseException("作业已超过截止时间，无法提交");
        }

        // 查询是否已提交
        LambdaQueryWrapper<HomeworkSubmission> wrapper = Wrappers.<HomeworkSubmission>lambdaQuery()
                .eq(HomeworkSubmission::getHomeworkId, dto.getHomeworkId())
                .eq(HomeworkSubmission::getStudentId, currentUser.getId());
        HomeworkSubmission existing = submissionMapper.selectOne(wrapper);

        if (existing != null && existing.getStatus() == 1) {
            // 已提交，更新
            if (dto.getContent() != null) {
                existing.setContent(dto.getContent());
            }
            if (file != null && !file.isEmpty()) {
                String fileUrl = saveAttachment(file);
                existing.setAttachmentUrl(fileUrl);
                existing.setAttachmentName(file.getOriginalFilename());
            }
            existing.setSubmitTime(LocalDateTime.now());
            // 重新提交后清空之前的成绩，需要重新批改
            existing.setScore(null);
            existing.setComment(null);
            existing.setGradeTime(null);
            submissionMapper.updateById(existing);
            log.info("学生[{}]重新提交作业[{}]", currentUser.getRealName(), dto.getHomeworkId());
        } else {
            // 首次提交
            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setHomeworkId(dto.getHomeworkId());
            submission.setStudentId(currentUser.getId());
            submission.setContent(dto.getContent());
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus(1);
            if (file != null && !file.isEmpty()) {
                String fileUrl = saveAttachment(file);
                submission.setAttachmentUrl(fileUrl);
                submission.setAttachmentName(file.getOriginalFilename());
            }
            submissionMapper.insert(submission);
            log.info("学生[{}]提交作业[{}]成功", currentUser.getRealName(), dto.getHomeworkId());
        }
    }

    /**
     * 教师批改作业
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeHomework(HomeworkGradeDTO dto) {
        User currentUser = getCurrentUser();
        if (!"teacher".equals(currentUser.getRole()) && !"admin".equals(currentUser.getRole())) {
            throw new BaseException("只有教师可以批改作业");
        }
        if (dto.getSubmissionId() == null) {
            throw new BaseException("提交记录ID不能为空");
        }
        if (dto.getScore() == null) {
            throw new BaseException("请输入成绩");
        }
        if (dto.getScore().compareTo(BigDecimal.ZERO) < 0
                || dto.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BaseException("成绩必须在0-100之间");
        }

        HomeworkSubmission submission = submissionMapper.selectById(dto.getSubmissionId());
        if (submission == null || submission.getStatus() == 0) {
            throw new BaseException("提交记录不存在");
        }
        // 权限校验：只有发布该作业的教师可以批改
        Homework homework = homeworkMapper.selectById(submission.getHomeworkId());
        if (homework == null) {
            throw new BaseException("关联作业不存在");
        }
        if (!homework.getTeacherId().equals(currentUser.getId())
                && !"admin".equals(currentUser.getRole())) {
            throw new BaseException("无权批改该作业");
        }

        submission.setScore(dto.getScore());
        submission.setComment(dto.getComment());
        submission.setGradeTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
        log.info("作业提交记录[{}]批改完成，成绩[{}]", dto.getSubmissionId(), dto.getScore());
    }

    /**
     * 管理员作业统计
     */
    @Override
    public HomeworkStatsVO getStats() {
        User currentUser = getCurrentUser();
        if (!"admin".equals(currentUser.getRole())) {
            throw new BaseException("只有管理员可以查看作业统计");
        }
        HomeworkStatsVO statsVO = new HomeworkStatsVO();

        Long totalCount = homeworkMapper.selectCount(
                Wrappers.<Homework>lambdaQuery().eq(Homework::getStatus, 1));
        Long submissionCount = submissionMapper.selectCount(
                Wrappers.<HomeworkSubmission>lambdaQuery().eq(HomeworkSubmission::getStatus, 1));
        Long gradedCount = submissionMapper.selectCount(
                Wrappers.<HomeworkSubmission>lambdaQuery()
                        .eq(HomeworkSubmission::getStatus, 1)
                        .isNotNull(HomeworkSubmission::getScore));

        statsVO.setTotalCount(totalCount);
        statsVO.setSubmissionCount(submissionCount);
        statsVO.setGradedCount(gradedCount);
        statsVO.setUngradedCount(submissionCount - (gradedCount == null ? 0 : gradedCount));
        return statsVO;
    }
}

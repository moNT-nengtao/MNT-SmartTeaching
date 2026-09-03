package com.smartteaching.service.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteaching.common.dto.notice.NoticeQueryDTO;
import com.smartteaching.common.dto.notice.NoticeSaveDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.SecurityUtils;
import com.smartteaching.common.vo.notice.NoticeListVO;
import com.smartteaching.entity.course.Course;
import com.smartteaching.entity.notice.Notice;
import com.smartteaching.entity.notice.NoticeReadRecord;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.NoticeMapper;
import com.smartteaching.mapper.NoticeReadRecordMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @ClassName NoticeServiceImpl
 * @Description 公告 Service 实现
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    /** 公告类型常量 */
    private static final String TYPE_SYSTEM = "system";
    private static final String TYPE_COURSE = "course";
    private static final String TYPE_NOTICE = "notice";

    @Resource
    private NoticeMapper noticeMapper;

    @Resource
    private NoticeReadRecordMapper readRecordMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CourseMapper courseMapper;

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
     * 校验公告类型合法性
     */
    private void checkType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new BaseException("请选择公告类型");
        }
        if (!TYPE_SYSTEM.equals(type) && !TYPE_COURSE.equals(type) && !TYPE_NOTICE.equals(type)) {
            throw new BaseException("公告类型不合法");
        }
    }

    /**
     * 校验发布权限：仅管理员/教师可发布公告
     */
    private void checkPublishPermission(User currentUser) {
        if (!"admin".equals(currentUser.getRole()) && !"teacher".equals(currentUser.getRole())) {
            throw new BaseException("只有管理员或教师可以发布公告");
        }
    }

    /**
     * 校验管理权限：发布人本人或管理员
     */
    private void checkManagePermission(Notice notice, User currentUser) {
        if ("admin".equals(currentUser.getRole())) {
            return;
        }
        if (notice.getPublisherId() != null && notice.getPublisherId().equals(currentUser.getId())) {
            return;
        }
        // 教师可管理自己授课课程的课程公告
        if ("teacher".equals(currentUser.getRole())
                && notice.getCourseId() != null) {
            Course course = courseMapper.selectById(notice.getCourseId());
            if (course != null && course.getTeacherId() != null
                    && course.getTeacherId().equals(currentUser.getId())) {
                return;
            }
        }
        throw new BaseException("无权操作该公告");
    }

    /**
     * 校验课程公告的关联课程：课程必须存在且启用；教师只能发自己授课的课程
     */
    private Course checkCourse(Long courseId, User currentUser) {
        if (courseId == null) {
            throw new BaseException("课程公告必须选择关联课程");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BaseException("关联课程不存在");
        }
        if (course.getStatus() != null && course.getStatus() == 0) {
            throw new BaseException("关联课程已被禁用");
        }
        if ("teacher".equals(currentUser.getRole())
                && (course.getTeacherId() == null || !course.getTeacherId().equals(currentUser.getId()))) {
            throw new BaseException("只能发布自己授课课程的公告");
        }
        return course;
    }

    /**
     * 分页查询当前用户可见的公告
     */
    @Override
    public PageResult<NoticeListVO> getNoticeList(NoticeQueryDTO dto) {
        User currentUser = getCurrentUser();
        long pageNum = dto.getPage() == null ? 1 : dto.getPage();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        IPage<NoticeListVO> iPage = new Page<>(pageNum, pageSize);
        IPage<NoticeListVO> voIPage = noticeMapper.selectNoticePage(
                iPage, dto, currentUser.getId(), currentUser.getRole());
        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 发布公告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(NoticeSaveDTO dto) {
        User currentUser = getCurrentUser();
        checkPublishPermission(currentUser);
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BaseException("请输入公告标题");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BaseException("请输入公告内容");
        }
        checkType(dto.getType());

        Notice notice = new Notice();
        notice.setTitle(dto.getTitle().trim());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setPublisherId(currentUser.getId());
        notice.setIsTop(dto.getIsTop() != null && dto.getIsTop() == 1 ? 1 : 0);
        notice.setStatus(1);
        notice.setPublishTime(LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);

        // 课程公告必须绑定课程
        if (TYPE_COURSE.equals(dto.getType())) {
            checkCourse(dto.getCourseId(), currentUser);
            notice.setCourseId(dto.getCourseId());
        } else {
            notice.setCourseId(null);
        }

        noticeMapper.insert(notice);
        log.info("用户[{}]发布公告[{}]成功，类型[{}]", currentUser.getRealName(), notice.getTitle(), dto.getType());
    }

    /**
     * 编辑公告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(NoticeSaveDTO dto) {
        User currentUser = getCurrentUser();
        if (dto.getId() == null) {
            throw new BaseException("公告ID不能为空");
        }
        Notice notice = noticeMapper.selectById(dto.getId());
        if (notice == null || notice.getStatus() == 0) {
            throw new BaseException("公告不存在或已撤回");
        }
        checkManagePermission(notice, currentUser);

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            notice.setTitle(dto.getTitle().trim());
        }
        if (dto.getContent() != null) {
            notice.setContent(dto.getContent());
        }
        if (dto.getType() != null) {
            checkType(dto.getType());
            notice.setType(dto.getType());
            // 课程公告必须绑定课程
            if (TYPE_COURSE.equals(dto.getType())) {
                checkCourse(dto.getCourseId(), currentUser);
                notice.setCourseId(dto.getCourseId());
            } else {
                notice.setCourseId(null);
            }
        }
        if (dto.getIsTop() != null) {
            notice.setIsTop(dto.getIsTop() == 1 ? 1 : 0);
        }
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        log.info("公告[{}]编辑成功", dto.getId());
    }

    /**
     * 撤回公告（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawNotice(Long id) {
        User currentUser = getCurrentUser();
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getStatus() == 0) {
            throw new BaseException("公告不存在或已撤回");
        }
        checkManagePermission(notice, currentUser);
        notice.setStatus(0);
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        log.info("公告[{}]已撤回", id);
    }

    /**
     * 置顶/取消置顶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long id, Integer isTop) {
        User currentUser = getCurrentUser();
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getStatus() == 0) {
            throw new BaseException("公告不存在或已撤回");
        }
        checkManagePermission(notice, currentUser);
        notice.setIsTop(isTop != null && isTop == 1 ? 1 : 0);
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        log.info("公告[{}]置顶状态更新为[{}]", id, notice.getIsTop());
    }

    /**
     * 公告详情（仅返回当前用户可见的公告）
     */
    @Override
    public NoticeListVO getNoticeDetail(Long id) {
        User currentUser = getCurrentUser();
        NoticeListVO vo = noticeMapper.selectNoticeDetail(id, currentUser.getId(), currentUser.getRole());
        if (vo == null) {
            throw new BaseException("公告不存在或无权查看");
        }
        return vo;
    }

    /**
     * 标记公告已读（幂等）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        User currentUser = getCurrentUser();
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getStatus() == 0) {
            throw new BaseException("公告不存在或已撤回");
        }
        // 幂等：已存在已读记录则跳过
        Long count = readRecordMapper.selectCount(
                Wrappers.<NoticeReadRecord>lambdaQuery()
                        .eq(NoticeReadRecord::getNoticeId, id)
                        .eq(NoticeReadRecord::getUserId, currentUser.getId()));
        if (count != null && count > 0) {
            return;
        }
        NoticeReadRecord record = new NoticeReadRecord();
        record.setNoticeId(id);
        record.setUserId(currentUser.getId());
        record.setReadTime(LocalDateTime.now());
        readRecordMapper.insert(record);
        log.info("用户[{}]已读公告[{}]", currentUser.getRealName(), id);
    }

    /**
     * 当前用户未读公告数
     */
    @Override
    public Long getUnreadCount() {
        User currentUser = getCurrentUser();
        Long count = noticeMapper.countUnread(currentUser.getId(), currentUser.getRole());
        return count == null ? 0L : count;
    }
}

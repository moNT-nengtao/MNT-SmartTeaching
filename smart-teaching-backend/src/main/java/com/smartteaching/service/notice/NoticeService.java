package com.smartteaching.service.notice;

import com.smartteaching.common.dto.notice.NoticeQueryDTO;
import com.smartteaching.common.dto.notice.NoticeSaveDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.notice.NoticeListVO;

/**
 * @ClassName NoticeService
 * @Description 公告 Service
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
public interface NoticeService {

    /**
     * 分页查询当前用户可见的公告
     */
    PageResult<NoticeListVO> getNoticeList(NoticeQueryDTO dto);

    /**
     * 发布公告（管理员/教师）
     */
    void publishNotice(NoticeSaveDTO dto);

    /**
     * 编辑公告（发布人本人或管理员）
     */
    void updateNotice(NoticeSaveDTO dto);

    /**
     * 撤回公告（软删除，发布人本人或管理员）
     */
    void withdrawNotice(Long id);

    /**
     * 置顶/取消置顶（发布人本人或管理员）
     */
    void toggleTop(Long id, Integer isTop);

    /**
     * 公告详情
     */
    NoticeListVO getNoticeDetail(Long id);

    /**
     * 标记公告已读
     */
    void markRead(Long id);

    /**
     * 当前用户未读公告数
     */
    Long getUnreadCount();
}

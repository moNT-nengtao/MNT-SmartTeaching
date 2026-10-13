package com.smartteaching.controller.notice;

import com.smartteaching.common.dto.notice.NoticeQueryDTO;
import com.smartteaching.common.dto.notice.NoticeSaveDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.notice.NoticeListVO;
import com.smartteaching.service.notice.NoticeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName NoticeController
 * @Description 通知公告控制器
 * @Author MNT
 * @Date 2026/8/29 21:13
 **/
@RestController
@RequestMapping("/api/notice")
@Slf4j
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 公告列表（按当前用户可见范围返回）
     */
    @GetMapping("/list")
    public Result<PageResult<NoticeListVO>> getNoticeList(NoticeQueryDTO queryDTO) {
        log.info("公告列表: {}", queryDTO);
        PageResult<NoticeListVO> page = noticeService.getNoticeList(queryDTO);
        return Result.success(page);
    }

    /**
     * 发布公告（管理员/教师）
     */
    @PostMapping
    public Result publishNotice(@RequestBody NoticeSaveDTO dto) {
        log.info("发布公告: {}", dto);
        noticeService.publishNotice(dto);
        return Result.success("发布公告成功");
    }

    /**
     * 编辑公告（发布人本人或管理员）
     */
    @PutMapping
    public Result updateNotice(@RequestBody NoticeSaveDTO dto) {
        log.info("编辑公告: {}", dto);
        noticeService.updateNotice(dto);
        return Result.success("编辑公告成功");
    }

    /**
     * 撤回公告（软删除）
     */
    @PutMapping("/{id}/withdraw")
    public Result withdrawNotice(@PathVariable Long id) {
        log.info("撤回公告: {}", id);
        noticeService.withdrawNotice(id);
        return Result.success("撤回公告成功");
    }

    /**
     * 置顶/取消置顶
     */
    @PutMapping("/{id}/top")
    public Result toggleTop(@PathVariable Long id, @RequestParam(required = false) Integer isTop) {
        log.info("置顶公告: id={}, isTop={}", id, isTop);
        noticeService.toggleTop(id, isTop);
        return Result.success("操作成功");
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public Result<NoticeListVO> getNoticeDetail(@PathVariable Long id) {
        log.info("公告详情: {}", id);
        return Result.success(noticeService.getNoticeDetail(id));
    }

    /**
     * 标记已读（幂等）
     */
    @PutMapping("/{id}/read")
    public Result markRead(@PathVariable Long id) {
        log.info("标记公告已读: {}", id);
        noticeService.markRead(id);
        return Result.success();
    }

    /**
     * 未读公告数量
     */
    @GetMapping("/unreadCount")
    public Result<Long> unreadCount() {
        log.info("未读公告数量");
        return Result.success(noticeService.getUnreadCount());
    }

}

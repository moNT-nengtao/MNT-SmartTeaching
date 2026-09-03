package com.smartteaching.controller.qa;

import com.smartteaching.common.dto.qa.QaQueryDTO;
import com.smartteaching.common.dto.qa.QaQuestionSaveDTO;
import com.smartteaching.common.dto.qa.QaReplyDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.qa.QaDetailVO;
import com.smartteaching.common.vo.qa.QaQuestionListVO;
import com.smartteaching.service.qa.QaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName QaController
 * @Description 答疑社区控制器
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@RestController
@RequestMapping("/api/qa")
@Slf4j
public class QaController {

    @Resource
    private QaService qaService;

    /**
     * 问题列表（按课程分区/标签/关键词筛选）
     */
    @GetMapping("/list")
    public Result<PageResult<QaQuestionListVO>> getQuestionList(QaQueryDTO queryDTO) {
        log.info("问题列表: {}", queryDTO);
        return Result.success(qaService.getQuestionList(queryDTO));
    }

    /**
     * 问题详情（问题 + 回复列表）
     */
    @GetMapping("/{id}")
    public Result<QaDetailVO> getQuestionDetail(@PathVariable Long id) {
        log.info("问题详情: {}", id);
        return Result.success(qaService.getQuestionDetail(id));
    }

    /**
     * 发布问题
     */
    @PostMapping
    public Result publishQuestion(@RequestBody QaQuestionSaveDTO dto) {
        log.info("发布问题: {}", dto);
        qaService.publishQuestion(dto);
        return Result.success("发布问题成功");
    }

    /**
     * 回复问题
     */
    @PostMapping("/reply")
    public Result replyQuestion(@RequestBody QaReplyDTO dto) {
        log.info("回复问题: {}", dto);
        qaService.replyQuestion(dto);
        return Result.success("回复成功");
    }

    /**
     * 点赞问题
     */
    @PutMapping("/{id}/like")
    public Result likeQuestion(@PathVariable Long id) {
        log.info("点赞问题: {}", id);
        qaService.likeQuestion(id);
        return Result.success();
    }

    /**
     * 点赞回复
     */
    @PutMapping("/reply/{replyId}/like")
    public Result likeReply(@PathVariable Long replyId) {
        log.info("点赞回复: {}", replyId);
        qaService.likeReply(replyId);
        return Result.success();
    }

    /**
     * 置顶/取消置顶问题（管理员/教师）
     */
    @PutMapping("/{id}/top")
    public Result toggleTop(@PathVariable Long id, @RequestParam(required = false) Integer isTop) {
        log.info("置顶问题: id={}, isTop={}", id, isTop);
        qaService.toggleTop(id, isTop);
        return Result.success("操作成功");
    }

    /**
     * 标签列表
     */
    @GetMapping("/tags")
    public Result<List<String>> getTags() {
        log.info("获取标签列表");
        return Result.success(qaService.getTags());
    }

}

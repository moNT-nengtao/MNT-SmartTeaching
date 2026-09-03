package com.smartteaching.service.qa;

import com.smartteaching.common.dto.qa.QaQueryDTO;
import com.smartteaching.common.dto.qa.QaQuestionSaveDTO;
import com.smartteaching.common.dto.qa.QaReplyDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.qa.QaDetailVO;
import com.smartteaching.common.vo.qa.QaQuestionListVO;

import java.util.List;

/**
 * @ClassName QaService
 * @Description 答疑社区 Service
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
public interface QaService {

    /**
     * 分页查询问题列表
     */
    PageResult<QaQuestionListVO> getQuestionList(QaQueryDTO dto);

    /**
     * 问题详情（问题 + 回复列表）
     */
    QaDetailVO getQuestionDetail(Long id);

    /**
     * 发布问题
     */
    void publishQuestion(QaQuestionSaveDTO dto);

    /**
     * 回复问题
     */
    void replyQuestion(QaReplyDTO dto);

    /**
     * 点赞问题
     */
    void likeQuestion(Long id);

    /**
     * 点赞回复
     */
    void likeReply(Long replyId);

    /**
     * 置顶/取消置顶问题（管理员/教师）
     */
    void toggleTop(Long id, Integer isTop);

    /**
     * 获取全部去重标签
     */
    List<String> getTags();
}

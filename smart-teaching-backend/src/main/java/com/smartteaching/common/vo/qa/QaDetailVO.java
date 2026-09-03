package com.smartteaching.common.vo.qa;

import lombok.Data;

import java.util.List;

/**
 * @ClassName QaDetailVO
 * @Description 问题详情 VO（问题 + 回复列表）
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class QaDetailVO {

    private QaQuestionListVO question;

    private List<QaReplyVO> replies;
}

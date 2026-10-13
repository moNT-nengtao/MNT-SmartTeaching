package com.smartteaching.common.dto.qa;

import lombok.Data;

/**
 * @ClassName QaReplyDTO
 * @Description 回复问题 DTO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class QaReplyDTO {

    private Long questionId;

    private String content;
}

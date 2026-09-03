package com.smartteaching.common.dto.notice;

import lombok.Data;

/**
 * @ClassName NoticeQueryDTO
 * @Description 公告查询 DTO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class NoticeQueryDTO {

    private Integer page;

    private Integer pageSize;

    private String keyword;

    /**
     * 已读状态筛选：null/空=全部, 0=未读, 1=已读
     */
    private Integer isRead;
}

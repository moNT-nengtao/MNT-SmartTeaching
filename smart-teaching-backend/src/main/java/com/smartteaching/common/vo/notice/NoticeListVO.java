package com.smartteaching.common.vo.notice;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName NoticeListVO
 * @Description 公告列表/详情 VO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class NoticeListVO {

    private Long id;

    private String title;

    private String content;

    private String type;

    private Long courseId;

    private String courseName;

    private Long publisherId;

    private String publisherName;

    private LocalDateTime publishTime;

    private Integer isTop;

    private Integer status;

    /**
     * 当前用户是否已读：0=未读, 1=已读
     */
    private Integer isRead;

    private LocalDateTime createTime;
}

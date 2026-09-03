package com.smartteaching.common.dto.notice;

import lombok.Data;

/**
 * @ClassName NoticeSaveDTO
 * @Description 公告新增/编辑 DTO
 * @Author MNT
 * @Date 2026/9/1 21:00
 **/
@Data
public class NoticeSaveDTO {

    private Long id;

    private String title;

    /**
     * 公告类型：system=全校公告, course=课程公告, notice=普通通知
     */
    private String type;

    /**
     * 关联课程ID（课程公告必填）
     */
    private Long courseId;

    /**
     * 是否置顶 0/1（前端传 boolean，此处按 Integer 接收）
     */
    private Integer isTop;

    private String content;
}

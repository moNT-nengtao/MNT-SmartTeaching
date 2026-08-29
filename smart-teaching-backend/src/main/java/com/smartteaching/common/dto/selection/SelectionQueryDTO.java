package com.smartteaching.common.dto.selection;

import lombok.Data;

/**
 * @ClassName SelectionQueryDTO
 * @Description 选课大厅查询DTO
 * @Author MNT
 * @Date 2026/8/17 15:33
 **/
@Data
public class SelectionQueryDTO {
    private Integer pageNum;
    private Integer pageSize;

    /** 课程名称模糊搜索 keyword */
    private String keyword;
}

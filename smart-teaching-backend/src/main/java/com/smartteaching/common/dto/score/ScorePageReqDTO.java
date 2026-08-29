package com.smartteaching.common.dto.score;

import lombok.Data;

/**
 * @ClassName ScoreAbnormalDTO
 * @Description 成绩分页请求DTO
 * @Author MNT
 * @Date 2026/8/27 12:06
 **/
@Data
public class ScorePageReqDTO {

    /**
     * 学期（如：2025-2026-2）
     */
    private String semester;

    /**
     * 学院ID
     */
    private Long collegeId;

    /**
     * 当前页码（默认1）
     */
    private Integer pageNum;

    /**
     * 每页条数（默认10）
     */
    private Integer pageSize;
}
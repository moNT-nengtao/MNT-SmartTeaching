package com.smartteaching.common.vo.homework;

import lombok.Data;

/**
 * @ClassName HomeworkStatsVO
 * @Description 管理员 作业统计 VO
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
public class HomeworkStatsVO {

    /**
     * 作业总数
     */
    private Long totalCount;

    /**
     * 提交总数
     */
    private Long submissionCount;

    /**
     * 已批改数
     */
    private Long gradedCount;

    /**
     * 未批改数
     */
    private Long ungradedCount;
}

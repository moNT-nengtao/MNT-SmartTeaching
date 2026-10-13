package com.smartteaching.common.vo.selection;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SelectionConflictVO
 * @Description
 * @Author MNT
 * @Date 2026/8/30 17:15
 **/
@Data
public class SelectionConflictVO {

    /**
     * 排课ID
     */
    private Long scheduleId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 星期几（1-7）
     */
    private Integer day;

    /**
     * 节次（1-6）
     */
    private Integer lesson;

    /**
     * 周次JSON字符串（如 "[1,2,3,4,5]"）
     */
    private String weekJson;

    /**
     * 周次列表（解析后的）
     */
    private List<Integer> weeks;

    /**
     * 教室
     */
    private String classroom;

    /**
     * 上课时间描述
     */
    private String timeDesc;
}
package com.smartteaching.common.dto.schedule;


import lombok.Data;

@Data
public class ScheduleQueryDTO {

    private Integer pageNum;
    private Integer pageSize;
    //现在搜索框只有一个课程，后续再考虑补
    /** 课程ID */
    private Long courseId;
    /** 课程name */
    private String courseName;
    /** 教师ID */
    private Long teacherId;
    /** 班级ID */
    private Long classId;
    /** 星期 1‑7 */
    private Integer day;
    /** 节次 */
    private Integer lesson;
}
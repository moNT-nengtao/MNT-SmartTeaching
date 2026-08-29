package com.smartteaching.common.vo.selection;

import lombok.Data;

/**
 * @ClassName SelectionMyCourseVO
 * @Description 我的已选课程返回VO
 * @Author MNT
 * @Date 2026/8/22 09:37
 **/
@Data
public class SelectionMyCourseVO {
    private Long courseId;
    private String code;
    private String week;
    private String courseName;
    private String teacherName;
    private Integer credit;
    private String scheduleTime;
    private String classroom;
    private Long selectRecordId;
}

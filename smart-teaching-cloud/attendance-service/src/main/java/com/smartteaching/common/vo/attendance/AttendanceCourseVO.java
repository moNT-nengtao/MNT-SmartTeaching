package com.smartteaching.common.vo.attendance;

import lombok.Data;

/**
 * @ClassName AttendanceCourseVO
 * @Description 教师可发起签到的课程选项VO
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceCourseVO {

    private Long id;

    private String courseName;
}

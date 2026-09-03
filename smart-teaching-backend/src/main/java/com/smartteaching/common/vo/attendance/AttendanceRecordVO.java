package com.smartteaching.common.vo.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @ClassName AttendanceRecordVO
 * @Description 学生个人考勤记录VO（学生端查看）
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceRecordVO {

    private Long recordId;

    private Long sessionId;

    private Long courseId;

    private String courseName;

    private String courseCode;

    private LocalDate sessionDate;

    private LocalDateTime checkinTime;

    /** 0=缺勤,1=考勤成功,2=迟到,3=请假,4=旷课 */
    private Integer status;

    private String statusText;
}

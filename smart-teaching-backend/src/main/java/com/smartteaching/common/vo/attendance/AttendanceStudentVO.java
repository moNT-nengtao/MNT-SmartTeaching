package com.smartteaching.common.vo.attendance;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName AttendanceStudentVO
 * @Description 签到会话学生名单VO（教师端查看/修改）
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceStudentVO {

    /** 考勤记录ID */
    private Long recordId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String className;

    /** 0=缺勤,1=考勤成功,2=迟到,3=请假,4=旷课 */
    private Integer status;

    private String statusText;

    private LocalDateTime checkinTime;
}

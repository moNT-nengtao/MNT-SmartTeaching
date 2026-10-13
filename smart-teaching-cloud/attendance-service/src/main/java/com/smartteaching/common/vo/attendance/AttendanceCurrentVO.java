package com.smartteaching.common.vo.attendance;

import lombok.Data;

/**
 * @ClassName AttendanceCurrentVO
 * @Description 学生当前待签到会话VO（进入签到页时展示）
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceCurrentVO {

    private Long sessionId;

    private Long courseId;

    private String courseName;

    private String teacherName;

    /** 签到有效时长（分钟） */
    private Integer duration;

    /** 剩余有效秒数 */
    private Long remainingSeconds;

    /** 当前学生是否已完成本次签到 */
    private Boolean checkedIn;

    /** 当前学生本次考勤状态（0缺勤/1考勤成功/2迟到/3请假/4旷课） */
    private Integer status;

    private String statusText;
}

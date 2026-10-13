package com.smartteaching.common.vo.attendance;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName AttendanceSessionVO
 * @Description 签到会话返回VO
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceSessionVO {

    private Long sessionId;

    private Long courseId;

    private String courseName;

    private Long teacherId;

    private String teacherName;

    private LocalDate sessionDate;

    /** 签到有效时长（分钟） */
    private Integer duration;

    /** 剩余有效秒数（Redis TTL，会话进行中才有值） */
    private Long remainingSeconds;

    /** 1=进行中,0=已结束 */
    private Integer status;

    /** 九宫格签到图案序列（0-8，用于前端回显展示） */
    private List<Integer> pattern;
}

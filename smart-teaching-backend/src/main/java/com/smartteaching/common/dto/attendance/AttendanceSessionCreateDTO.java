package com.smartteaching.common.dto.attendance;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * @ClassName AttendanceSessionCreateDTO
 * @Description 创建签到会话DTO
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceSessionCreateDTO {

    /** 课程ID */
    private Long courseId;

    /** 签到有效时长（分钟，服务端强制上限20分钟） */
    private Integer duration;

    /** 九宫格签到图案序列（0-8，至少3个点） */
    private List<Integer> pattern;

    /** 课程开始时间（可选） */
    private LocalTime startTime;

    /** 课程结束时间（可选） */
    private LocalTime endTime;
}

package com.smartteaching.common.dto.attendance;

import lombok.Data;

/**
 * @ClassName AttendanceStatusUpdateDTO
 * @Description 教师修改考勤状态DTO（仅允许迟到/请假/旷课，不允许改为考勤成功）
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceStatusUpdateDTO {

    /** 考勤记录ID */
    private Long recordId;

    /** 目标状态：2=迟到,3=请假,4=旷课（不允许改为考勤成功） */
    private Integer status;
}

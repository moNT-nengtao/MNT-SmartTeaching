package com.smartteaching.common.vo.attendance;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AttendanceSessionDetailVO
 * @Description 签到会话详情VO（会话信息 + 学生名单 + 统计）
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceSessionDetailVO {

    private AttendanceSessionVO session;

    private List<AttendanceStudentVO> records;

    private AttendanceStatsVO stats;
}

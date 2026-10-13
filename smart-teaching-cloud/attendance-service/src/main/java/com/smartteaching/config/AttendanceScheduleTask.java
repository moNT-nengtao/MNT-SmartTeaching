package com.smartteaching.config;

import com.smartteaching.service.attendance.AttendanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @ClassName AttendanceScheduleTask
 * @Description 考勤定时任务：每分钟兜底结束已超时的签到会话，
 * 将缺勤落定为旷课并联动生成/刷新学业预警中的旷课警告
 * @Author MNT
 * @Date 2026/9/2
 **/
@Component
@Slf4j
public class AttendanceScheduleTask {

    @Resource
    private AttendanceService attendanceService;

    /**
     * 每分钟执行一次，结束超时未结束的签到会话
     */
    @Scheduled(cron = "0 * * * * ?")
    public void finalizeExpiredSessions() {
        try {
            attendanceService.finalizeExpiredSessions();
        } catch (Exception e) {
            log.error("定时结束超时签到会话异常", e);
        }
    }
}

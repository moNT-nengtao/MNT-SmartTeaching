package com.smartteaching.service.service;

import com.smartteaching.common.vo.schedule.ScheduleWeeklyQueryVO;

/**
 * @ClassName ScheduleService
 * @Description
 * @Author MNT
 * @Date 2026/8/30 16:34
 **/
public interface ScheduleService {
    /**
     * 周课表
     *
     * @param studentId
     * @param week
     * @return
     */
    ScheduleWeeklyQueryVO getScheduleWeeklyQuery(Long studentId, Long week);

}

package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteaching.common.vo.schedule.ScheduleWeeklyQueryVO;
import com.smartteaching.entity.course.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName ScheduleMapper
 * @Description
 * @Author MNT
 * @Date 2026/8/30 16:36
 **/
@Mapper
public interface ScheduleMapper extends BaseMapper<CourseSchedule> {
    /**
     * 周列表
     * @param studentId
     * @param week
     * @return
     */
    List<ScheduleWeeklyQueryVO.CourseItem> getScheduleWeeklyQuery(
            @Param("studentId") Long studentId,
            @Param("week") Long week);
}

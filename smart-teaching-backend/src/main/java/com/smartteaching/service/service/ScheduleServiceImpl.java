package com.smartteaching.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartteaching.common.constant.LessonSectionEnum;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.utils.WeekUtil;
import com.smartteaching.common.vo.schedule.ScheduleWeeklyQueryVO;
import com.smartteaching.entity.course.CourseSchedule;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.ScheduleMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName ScheduleServiceImpl
 * @Description
 * @Author MNT
 * @Date 2026/8/30 16:34
 **/
@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 周课表
     *
     * @param studentId
     * @param week
     * @return
     */
    @Override
    public ScheduleWeeklyQueryVO getScheduleWeeklyQuery(Long studentId, Long week) {
        checkStudentUser(studentId);

        // 校验周次
        if (week == null || week < 1 || week > 20) {
            throw new BaseException("请确认周次是否正确（1-20周）");
        }

        // 查询课表
        List<ScheduleWeeklyQueryVO.CourseItem> items = scheduleMapper.getScheduleWeeklyQuery(studentId, week);

        // 数据转换（周次JSON → 范围字符串，节次 → 上课时间）
        for (ScheduleWeeklyQueryVO.CourseItem item : items) {
            // 周次转换
            if (item.getWeekJson() != null) {
                String weekRange = WeekUtil.jsonToRangeStr(item.getWeekJson());
                item.setWeekRange(weekRange);
            }
            // 上课时间
            if (item.getLesson() != null) {
                String timeDesc = LessonSectionEnum.getTimeDesc(item.getLesson());
                item.setTime(timeDesc);
            }
        }

        // 5. 封装返回
        ScheduleWeeklyQueryVO vo = new ScheduleWeeklyQueryVO();
        vo.setCourses(items);

        log.info("学生 {} 查询第 {} 周课表，共 {} 门课", studentId, week, items.size());
        return vo;
    }



    /**
     * 检查用户
     * @param studentId
     */
    public void checkStudentUser(Long studentId){
        User user = userMapper.selectById(studentId);
        if(user == null){
            throw new BaseException(MessageConstant.OPERATE_USER_NOT_EXIST);
        }
        if(!"student".equals(user.getRole())){
            throw new BaseException(MessageConstant.USER_ILLEGAL);
        }
    }
}

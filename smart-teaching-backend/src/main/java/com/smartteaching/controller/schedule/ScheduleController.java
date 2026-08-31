package com.smartteaching.controller.schedule;

import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.schedule.ScheduleWeeklyQueryVO;
import com.smartteaching.service.service.ScheduleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName ScheduleController
 * @Description
 * @Author MNT
 * @Date 2026/8/30 16:32
 **/
@RestController
@RequestMapping("/api/schedule")
@Slf4j
public class ScheduleController {
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 周课表
     * @return
     */
    @GetMapping("/week")
    public Result<List<ScheduleWeeklyQueryVO.CourseItem>> getScheduleWeeklyQueryVO(
            @RequestParam("week") Long week,
            HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long studentId = jwtUtil.getUserIdFromHeader(authHeader);
        log.info("获取周课表, studentId：{}, week：{}", studentId, week);

        ScheduleWeeklyQueryVO vo = scheduleService.getScheduleWeeklyQuery(studentId, week);

        return Result.success(vo.getCourses());
    }

    /**
     * 下一节课(暂时不考虑实现,功能)
     * @return
     */
    @GetMapping("/next")
    public Result NextSchedule(){
        return Result.success();
    }

}

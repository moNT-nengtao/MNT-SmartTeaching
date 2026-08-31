package com.smartteaching.controller.dashboard;

import com.alibaba.fastjson2.JSON;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.dashborad.DashboardAdminVO;
import com.smartteaching.common.vo.dashborad.DashboardStudentVO;
import com.smartteaching.common.vo.dashborad.DashboardTeacherVO;
import com.smartteaching.service.dashboard.DashboardService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.excel.converters.ConverterKeyBuild.buildKey;

@RestController
@RequestMapping("/api/dashboard")
@Slf4j
public class DashboardController {

    @Resource
    private DashboardService dashboardService;
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 管理员
     * @return
     */
    @GetMapping("/admin")
    public Result<DashboardAdminVO> adminDashboard() {
        log.info("管理员仪表盘");
        DashboardAdminVO dashboardAdminVO = dashboardService.getAdminDashboard();
        return Result.success(dashboardAdminVO) ;
    }

    /**
     * 教师
     * @return
     */
    @GetMapping("/teacher")
    public Result<DashboardTeacherVO> teacherDashboard(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long teacherId = jwtUtil.getUserIdFromHeader(authHeader);
        log.info("教师仪表盘: {}", teacherId);

        DashboardTeacherVO dashboardTeacherVO = dashboardService.getTeacherDashboard(teacherId);
        return Result.success(dashboardTeacherVO) ;
    }

    /**
     * 学生
     * @return
     */
    @GetMapping("/student")
    public Result<DashboardStudentVO> studentDashboard(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        Long studentId = jwtUtil.getUserIdFromHeader(authHeader);
        log.info("学生仪表盘: {}", studentId);
        DashboardStudentVO dashboardStudentVO = dashboardService.getStudentDashboard(studentId);
        return Result.success(dashboardStudentVO) ;
    }
}

package com.smartteaching.controller.dashboard;

import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.dashborad.DashboardAdminVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /**
     * 管理员
     * @return
     */
    @GetMapping("/admin")
    public Result<DashboardAdminVO> adminDashboard() {
      return null;
    }

    /**
     * 教师
     * @return
     */
    @GetMapping("/teacher")
    public Result<Map<String, Object>> teacherDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("courseCount", 0);
        data.put("studentCount", 0);
        data.put("avgScore", 0);
        data.put("rating", 0);
        data.put("courseScoreDistribution", new Object[0]);
        data.put("evaluationScores", new Object[0]);
        return Result.success(data);
    }

    /**
     * 学生
     * @return
     */
    @GetMapping("/student")
    public Result<Map<String, Object>> studentDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("gpa", 0);
        data.put("creditCount", 0);
        data.put("attendanceRate", 0);
        data.put("failedCourseCount", 0);
        data.put("scoreRadar", new Object[0]);
        data.put("gpaTrend", new Object[0]);
        data.put("attendanceTrend", new Object[0]);
        data.put("homeworkTrend", new Object[0]);
        return Result.success(data);
    }
}

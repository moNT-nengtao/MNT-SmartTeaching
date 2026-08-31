package com.smartteaching.service.dashboard;

import com.smartteaching.common.vo.dashborad.DashboardAdminVO;
import com.smartteaching.common.vo.dashborad.DashboardStudentVO;
import com.smartteaching.common.vo.dashborad.DashboardTeacherVO;

/**
 * @ClassName DashboardService
 * @Description
 * @Author MNT
 * @Date 2026/8/27 09:50
 **/
public interface DashboardService {
    /**
     * 管理员仪表盘
     * @return
     */
    DashboardAdminVO getAdminDashboard();


    /**
     * 教师仪表盘
     * @return
     */
    DashboardTeacherVO getTeacherDashboard(Long teacherId);

    /**
     * 学生仪表盘
     * @return
     */
    DashboardStudentVO getStudentDashboard(Long studentId);
}

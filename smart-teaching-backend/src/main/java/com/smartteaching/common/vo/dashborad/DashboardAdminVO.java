package com.smartteaching.common.vo.dashborad;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName DashboardAdminVO
 * @Description
 * @Author MNT
 * @Date 2026/8/27 09:42
 **/
@Data
public class DashboardAdminVO {

    /**
     * 统计数据卡片
     */
    private StatCards statCards;

    /**
     * 各学院学生人数分布（柱状图）
     */
    private ChartData<String, Long> collegeStudentDistribution;

    /**
     * 师生比例（饼图）
     */
    private ChartData<String, Double> teacherStudentRatio;

    /**
     * 近7日系统活跃度（折线图）
     */
    private ChartData<String, Long> weeklyActivity;

    /**
     * 统计数据卡片内部类
     */
    @Data
    public static class StatCards {
        /**
         * 班级总数
         */
        private Long classCount;

        /**
         * 教师人数
         */
        private Long teacherCount;

        /**
         * 学生人数
         */
        private Long studentCount;

        /**
         * 课程总数
         */
        private Long courseCount;

        /**
         * 选课率（百分比）
         */
        private BigDecimal selectionRate;

        /**
         * 考勤合格率（百分比）
         */
        private BigDecimal attendanceRate;
    }

    /**
     * 图表数据通用结构
     * @param <X> X轴数据类型（如 String 表示分类名称）
     * @param <Y> Y轴数据类型（如 Long 表示数量，Double 表示比例）
     */
    @Data
    public static class ChartData<X, Y> {
        /**
         * X轴数据（维度名称列表）
         */
        private List<X> categories;

        /**
         * 系列数据
         */
        private List<Series<Y>> series;
    }

    /**
     * 图表系列数据
     * @param <Y> 数值类型
     */
    @Data
    public static class Series<Y> {
        /**
         * 系列名称
         */
        private String name;

        /**
         * 系列数据值列表
         */
        private List<Y> data;
    }
}
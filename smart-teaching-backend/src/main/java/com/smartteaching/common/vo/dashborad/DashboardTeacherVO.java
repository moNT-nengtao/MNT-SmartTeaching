package com.smartteaching.common.vo.dashborad;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName DashboardTeacherVO
 * @Description
 * @Author MNT
 * @Date 2026/8/29 16:09
 **/
@Data
public class DashboardTeacherVO {
    /**
     * 统计卡片
     */
    private TeacherStatCards statCards;

    /**
     * 所授课程成绩分布 柱状图
     */
    private ChartSeriesVO scoreDistribution;

    /**
     * 近7日考勤签到率趋势 折线图
     */
    private ChartSeriesVO attendanceTrend;

    @Data
    public static class TeacherStatCards {
        /**
         * 授课课程数
         */
        private Integer courseCount;
        /**
         * 选课总人数
         */
        private Integer studentTotal;
        /**
         * 所授课程平均分
         */
        private BigDecimal avgScore;
        /**
         * 教学平均评价分数
         */
        private BigDecimal avgEvaluate;
    }

    /**
     * 柱状图：课程成绩分布
     */
    @Data
    public static class ChartSeriesVO {
        private List<String> categories;
        private List<SeriesItem> series;

        @Data
        public static class SeriesItem {
            private String name;
            private List<?> data;
        }
    }


}
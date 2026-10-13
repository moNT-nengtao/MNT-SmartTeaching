package com.smartteaching.common.vo.dashborad;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName DashboardStudentVO
 * @Description
 * @Author MNT
 * @Date 2026/8/29 16:39
 **/
@Data
public class DashboardStudentVO {
    /**
     * 统计卡片
     */
    private StudentStatCards statCards;

    /**
     * 各科成绩雷达图
     */
    private RadarChartVO subjectRadar;

    /**
     * 绩点趋势折线图
     */
    private LineChartVO gpaTrend;

    /**
     * 月度考勤统计
     */
    private ChartSeriesVO attendanceMonth;

    /**
     * 各科成绩对比
     */
    private ChartSeriesVO scoreCompare;

    @Data
    public static class StudentStatCards {
        private BigDecimal gpa;
        private BigDecimal finishedCredit;
        private BigDecimal attendanceRate;
        private Integer failSubjectCount;
    }

    @Data
    public static class RadarChartVO {
        private List<RadarIndicator> indicator;
        private List<RadarData> data;

        @Data
        public static class RadarIndicator {
            private String name;
            private Integer max;
        }

        @Data
        public static class RadarData {
            private String name;
            private List<BigDecimal> value;
        }
    }

    @Data
    public static class LineChartVO {
        private List<String> categories;
        private List<SeriesItem> series;

        @Data
        public static class SeriesItem {
            private String name;
            private List<BigDecimal> data;
        }
    }

    @Data
    public static class ChartSeriesVO {
        private List<String> categories;
        private List<SeriesItem> series;

        @Data
        public static class SeriesItem {
            private String name;
            private List<BigDecimal> data;
        }
    }
}
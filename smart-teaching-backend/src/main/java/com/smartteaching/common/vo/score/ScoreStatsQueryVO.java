package com.smartteaching.common.vo.score;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ScoreStatsQueryVO
 * @Description 成绩统计返回VO
 * @Author MNT
 * @Date 2026/8/27 11:31
 **/
@Data
public class ScoreStatsQueryVO {

    /**
     * 统计卡片数据
     */
    private StatCardData statCards;

    /**
     * 成绩分布（饼图数据）
     */
    private ScoreDistribution pieData;

    /**
     * 各学院平均分对比（柱状图数据）
     */
    private CollegeScoreCompare barData;

    /**
     * 统计卡片数据
     */
    @Data
    public static class StatCardData {
        /**
         * 总人数
         */
        private Long totalStudents;
        /**
         * 平均分
         */
        private BigDecimal avgScore;
        /**
         * 及格率（百分比，如 85.5 表示85.5%）
         */
        private BigDecimal passRate;
        /**
         * 挂科人数
         */
        private Long failCount;
        /**
         * 总成绩记录数（用于后端计算及格率，不返回前端）
         */
        private Long totalRecord;
        /**
         * 及格记录数（后端计算用）
         */
        private Long passStudent;
    }

    /**
     * 成绩分布数据（饼图）
     */
    @Data
    public static class ScoreDistribution {
        /**
         * 优秀人数（>=90）
         */
        private Long excellentCount;
        /**
         * 良好人数（80-89）
         */
        private Long goodCount;
        /**
         * 中等人数（70-79）
         */
        private Long mediumCount;
        /**
         * 及格人数（60-69）
         */
        private Long passCount;
        /**
         * 不及格人数（<60）
         */
        private Long failCount;
        /**
         * 图表数据（用于前端ECharts饼图）
         */
        private List<ChartItem> chartData;
    }

    /**
     * 各学院平均分对比数据（柱状图）
     */
    @Data
    public static class CollegeScoreCompare {
        /**
         * 学院名称列表（X轴）
         */
        private List<String> colleges;
        /**
         * 各学院平均分列表（Y轴）
         */
        private List<BigDecimal> avgScores;
        /**
         * 各学院人数列表
         */
        private List<Long> studentCounts;

        /**
         * Mapper查询中间接收对象
         */
        @Data
        public static class Item {
            private String colleges;
            private BigDecimal avgScores;
            private Long studentCounts;
        }
    }

    /**
     * 异常成绩记录
     */
    @Data
    public static class AbnormalScore {
        /**
         * 学号
         */
        private String studentNo;
        /**
         * 姓名
         */
        private String studentName;
        /**
         * 课程名称
         */
        private String courseName;
        /**
         * 总评成绩
         */
        private BigDecimal totalScore;
        /**
         * 异常原因
         */
        private String reason;
    }

    /**
     * 图表数据项（通用）
     */
    @Data
    public static class ChartItem {
        /**
         * 名称（如：优秀、良好）
         */
        private String name;
        /**
         * 数值
         */
        private Long value;
    }
}
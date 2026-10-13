package com.smartteaching.common.vo.evaluation;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName EvaluationTeacherRankingVO
 * @Description
 * @Author MNT
 * @Date 2026/8/30 22:14
 **/
@Data
public class EvaluationTeacherVO {

    /**
     * 统计卡片
     */
    private StatCards statCards;

    /**
     * 雷达图各维度数据
     */
    private RadarData radarData;

    /**
     * 教师授课课程列表
     */
    private List<CourseItem> courseList;


    // ==================== 内部类 ====================

    @Data
    public static class StatCards {
        private BigDecimal avgScore;
        private Integer evaluationCount;
        private BigDecimal goodRate;
        private Integer courseCount;
    }

    @Data
    public static class RadarData {
        private BigDecimal teachingAbility;
        private BigDecimal classAtmosphere;
        private BigDecimal knowledgeClarity;
        private BigDecimal homeworkFeedback;
        private BigDecimal qaService;
    }

    @Data
    public static class CourseItem {
        private Long id;
        private String courseName;
        private String semester;
        private Integer studentCount;
        private Integer evaluationCount;
        private BigDecimal avgScore;
    }

    /**
     * 评价项（用于列表查询）
     */
    @Data
    public static class EvaluationItem {
        private Long id;
        private Long courseId;
        private String studentName;
        private BigDecimal score;
        private String comment;
        private LocalDateTime createTime;
        private BigDecimal teachingAbility;
        private BigDecimal classAtmosphere;
        private BigDecimal knowledgeClarity;
        private BigDecimal homeworkFeedback;
        private BigDecimal qaService;
    }
}
package com.smartteaching.common.vo.selection;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName SelectionQueryVO
 * @Description 选课大厅课程返回VO
 * @Author MNT
 * @Date 2026/8/22 10:03
 **/
@Data
public class SelectionQueryVO {
    private Long courseId;
    private String courseName;
    private String teacherName;
    private String code;
    /** 数据库字段 capacity 选课容量，映射给前端maxStudents */
    private Integer maxStudents;
    private Integer selectedCount;
    // 选课率 前端计算： selectedCount / maxStudents，后端不用返回

    /** 推荐理由（仅智能推荐时返回） */
    private String reason;
    /** 上课时间 */
    private String scheduleTime;
    /** 学分 */
    private Integer credit;
    /** 学时 */
    private Integer hours;
    /** 平均评分（0-5分） */
    private Double avgScore;
    /** 是否已选（true=已选，false=未选） */
    private Boolean isSelected;
    /** 剩余名额（可直接返回，前端也可计算） */
    private Integer remaining;

    /**
     * 授课能力
     */
    private BigDecimal teachingAbility;

    /**
     * 课堂氛围
     */
    private BigDecimal classAtmosphere;

    /**
     * 知识讲解清晰度
     */
    private BigDecimal knowledgeClarity;

    /**
     * 作业批改反馈
     */
    private BigDecimal homeworkFeedback;

    /**
     * 答疑服务
     */
    private BigDecimal qaService;

    /**
     * 最新评价内容
     */
    private String latestComment;

    /**
     * 评价人数
     */
    private Integer evaluationCount;
}

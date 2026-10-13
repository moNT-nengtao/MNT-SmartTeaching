package com.smartteaching.common.vo.evaluation;

import lombok.Data;

/**
 * @ClassName EvaluationCourseVO
 * @Description
 * @Author MNT
 * @Date 2026/8/30 22:13
 **/
@Data
public class EvaluationCourseVO {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 授课教师姓名
     */
    private String teacherName;

    /**
     * 学期
     */
    private String semester;

    /**
     * 是否已评价
     */
    private Boolean isEvaluated;

    /**
     * 是否可以评价
     */
    private Boolean canEvaluate;
}
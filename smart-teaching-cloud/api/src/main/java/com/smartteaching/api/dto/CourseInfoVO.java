package com.smartteaching.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 课程信息内部 VO（跨服务传递课程基础信息）
 * 由 course-service 提供，供考勤/选课/成绩/评价等服务 Feign 调用
 */
@Data
public class CourseInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 课程ID */
    private Long id;

    /** 课程名称 */
    private String name;

    /** 授课教师ID */
    private Long teacherId;

    /** 课程状态：1启用 0停用 */
    private Integer status;

    /** 开课学期 */
    private String semester;
}

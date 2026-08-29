package com.smartteaching.common.vo.course;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseQueryVO {
    /**主键id*/
    private Long id;
    /**课程编码*/
    private String code;
    /**课程名称*/
    private String name;
    /**授课教师id*/
    private Long teacherId;
    /**教师姓名（关联查询，数据库无列）*/
    private String teacherName;
    /**学分*/
    private BigDecimal credit;
    /**学期*/
    private String semester;
    /**课程最大容量*/
    private Integer capacity;
    /**课程描述*/
    private String description;
    /**状态 1启用 0禁用*/
    private Integer status;
    /**创建时间*/
    private LocalDateTime createTime;
    /**更新时间*/
    private LocalDateTime updateTime;
}

package com.smartteaching.common.vo.course;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseScheduleQueryVO {

    private Long id;

    /**课程id*/
    private Long courseId;
    /**课程名称，联表查询*/
    private String courseName;

    /**教师id*/
    private Long teacherId;
    /**教师姓名，联表查询*/
    private String teacherName;

    /**班级id*/
    private Long classId;
    /**班级名称，联表查询*/
    private String className;

    /**周次（第几教学周，例如3代表第3周）*/
    private String week;
    /**星期 1~7，周一到周日*/
    private Integer day;
    /**节次 1,2,3...*/
    private Integer lesson;
    /**教室*/
    private String room;

    /**课程颜色*/
    private String color;
    /**状态：1启用 0禁用*/
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

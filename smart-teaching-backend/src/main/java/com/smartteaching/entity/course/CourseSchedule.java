package com.smartteaching.entity.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName CourseSchedule
 * @Description 课程排课实体类，对应course_schedule表，管理课程的时间、地点与班级安排
 * @Author MNT
 * @Date 2026/8/14 09:47
 **/
@Data
@TableName("course_schedule")
public class CourseSchedule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long teacherId;

    private Long classId;

    private String week;

    private Integer day;

    private Integer lesson;

    private String room;

    private String color;

    private Integer status;
}

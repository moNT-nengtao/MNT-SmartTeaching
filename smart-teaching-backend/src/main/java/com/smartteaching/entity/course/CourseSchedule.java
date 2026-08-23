package com.smartteaching.entity.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

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

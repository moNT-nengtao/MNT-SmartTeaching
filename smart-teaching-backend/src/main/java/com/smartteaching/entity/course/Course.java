package com.smartteaching.entity.course;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName Course
 * @Description 课程实体类，对应course表，管理课程基本信息与容量状态
 * @Author MNT
 * @Date 2026/8/14 14:05
 **/
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private Long teacherId;

    private BigDecimal credit;

    private String semester;

    private Integer capacity;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
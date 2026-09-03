package com.smartteaching.entity.homework;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName Homework
 * @Description 作业实体类，对应 homework 表
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
@Data
@TableName("homework")
public class Homework {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long teacherId;

    private String title;

    private String content;

    private String attachmentUrl;

    private String attachmentName;

    private LocalDateTime deadline;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

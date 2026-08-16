package com.smartteaching.entity.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("course")
public class Course extends BaseEntity {

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
}

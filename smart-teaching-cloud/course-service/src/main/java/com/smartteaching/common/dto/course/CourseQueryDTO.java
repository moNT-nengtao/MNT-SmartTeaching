package com.smartteaching.common.dto.course;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class CourseQueryDTO {

    private Long id;

    private Long pageNum;

    private Long pageSize;

    private String keyword;

    private String code;

    private String name;

    private Long teacherId;

    private String semester;

    private Integer status;

    private BigDecimal credit;

    private Integer capacity;

    private String description;
}
package com.smartteaching.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleSaveDTO {

    private Long id;

    @NotNull(message = "请选择课程")
    private Long courseId;

    @NotNull(message = "请选择授课教师")
    private Long teacherId;

    @NotNull(message = "请选择班级")
    private Long classId;

    @NotNull(message = "请选择星期")
    private Integer day;

    @NotNull(message = "请选择节次")
    private Integer lesson;

    @NotBlank(message = "教室不能为空")
    private String room;

    @NotBlank(message = "请填写周次范围")
    private String week;

    private String color;
}
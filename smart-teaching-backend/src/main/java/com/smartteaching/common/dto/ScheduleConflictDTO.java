package com.smartteaching.common.dto;

import lombok.Data;

@Data
public class ScheduleConflictDTO {
    private Long id;
    private Long courseId;
    private Long teacherId;
    private Long classId;
    private String tempId;
    private String week;
    private Integer day;
    private Integer lesson;
    private String room;
    private String color;

}
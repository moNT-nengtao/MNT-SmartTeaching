package com.smartteaching.entity.attendance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@TableName("attendance_session")
public class AttendanceSession extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long teacherId;

    private Long classId;

    private LocalDate sessionDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String checkCode;

    private Integer status;
}

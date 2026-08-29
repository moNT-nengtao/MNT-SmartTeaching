package com.smartteaching.entity.attendance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @ClassName AttendanceSession
 * @Description 考勤场次实体类，对应attendance_session表，管理每次考勤的课程与时间安排
 * @Author MNT
 * @Date 2026/8/14 10:33
 **/
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

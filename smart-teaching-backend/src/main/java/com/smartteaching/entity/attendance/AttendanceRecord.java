package com.smartteaching.entity.attendance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName AttendanceRecord
 * @Description 考勤记录实体类，对应attendance_record表，存储学生签到打卡记录
 * @Author MNT
 * @Date 2026/8/14 13:52
 **/
@Data
@TableName("attendance_record")
public class AttendanceRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long studentId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private LocalDateTime checkinTime;

    private Integer status;
}

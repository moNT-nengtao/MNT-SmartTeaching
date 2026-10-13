package com.smartteaching.common.dto.attendance;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName AttendanceCheckinDTO
 * @Description 学生签到DTO
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceCheckinDTO {

    /** 九宫格签到图案序列（0-8） */
    private List<Integer> pattern;

    private BigDecimal longitude;

    private BigDecimal latitude;
}

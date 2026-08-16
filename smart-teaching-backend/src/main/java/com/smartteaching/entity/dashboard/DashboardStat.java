package com.smartteaching.entity.dashboard;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("dashboard_stat")
public class DashboardStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String statType;

    private LocalDate targetDate;

    private Long targetId;

    private Object value;

    private LocalDateTime createTime;
}

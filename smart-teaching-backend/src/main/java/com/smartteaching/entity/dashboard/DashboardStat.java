package com.smartteaching.entity.dashboard;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @ClassName DashboardStat
 * @Description 仪表盘统计数据实体类，对应dashboard_stat表，存储各类统计分析指标
 * @Author MNT
 * @Date 2026/8/14 11:28
 **/
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

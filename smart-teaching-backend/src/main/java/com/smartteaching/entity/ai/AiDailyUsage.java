package com.smartteaching.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

/**
 * @ClassName AiDailyUsage
 * @Description AI每日调用统计实体类，对应ai_daily_usage表，用于实现每日问答次数限制
 * @Author MNT
 * @Date 2026/9/2 23:10
 **/
@Data
@TableName("ai_daily_usage")
public class AiDailyUsage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate useDate;

    private Integer useCount;
}

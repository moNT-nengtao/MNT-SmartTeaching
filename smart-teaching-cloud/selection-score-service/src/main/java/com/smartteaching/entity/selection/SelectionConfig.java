package com.smartteaching.entity.selection;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName SelectionConfig
 * @Description 选课配置实体类，对应selection_config表，管理选课时间窗口及学分范围限制
 * @Author MNT
 * @Date 2026/8/14 09:36
 **/
@Data
@TableName("selection_config")
public class SelectionConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal minCredit;

    private BigDecimal maxCredit;

    private String scopeType;

    private String scopeValue;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

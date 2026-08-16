package com.smartteaching.entity.selection;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("selection_config")
public class SelectionConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal minCredit;

    private BigDecimal maxCredit;

    private String allowedMajors;

    private Integer status;
}

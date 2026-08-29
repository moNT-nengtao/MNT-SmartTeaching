package com.smartteaching.common.dto.selection;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * @ClassName SelectionConfigSaveDTO
 * @Description 选课配置保存DTO，/selection/time POST入参
 * @Author MNT
 * @Date 2026/8/17 14:52
 **/
@Data
public class SelectionConfigSaveDTO {

    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal minCredit;

    private BigDecimal maxCredit;

    /**
     * 允许专业数组JSON字符串 "[1,2]"
     */
    private String allowedMajors;

    /**
     * 选课开关：0关闭 1开启
     */
    private Integer status;
}

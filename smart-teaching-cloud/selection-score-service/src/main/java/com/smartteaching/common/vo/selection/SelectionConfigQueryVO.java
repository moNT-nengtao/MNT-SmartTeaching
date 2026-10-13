package com.smartteaching.common.vo.selection;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName SelectionConfigQueryVO
 * @Description 选课配置返回VO
 * @Author MNT
 * @Date 2026/8/22 09:14
 **/
@Data
public class SelectionConfigQueryVO {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private BigDecimal minCredit;

    private BigDecimal maxCredit;

    /**
     * 允许专业 JSON字符串: [1,2]
     */
    private String allowedMajors;

    /**
     * 0关闭 1开启
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
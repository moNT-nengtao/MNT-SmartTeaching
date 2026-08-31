package com.smartteaching.common.vo.warning;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName WarningListVO
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:42
 **/
@Data
public class WarningListVO {

    private Long id;

    private String title;

    private String studentName;

    private String studentNo;

    private String className;

    private Integer level;

    private String warningType;

    private String reason;

    private LocalDateTime createTime;

    private Integer status;
}
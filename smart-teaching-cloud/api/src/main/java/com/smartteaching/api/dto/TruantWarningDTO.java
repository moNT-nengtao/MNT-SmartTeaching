package com.smartteaching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 旷课预警创建 DTO
 * 考勤服务计算好旷课事实后，通过 Feign 传给 warning-service 落库
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruantWarningDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 学生用户ID */
    private Long userId;

    /** 学生姓名 */
    private String studentName;

    /** 课程名称 */
    private String courseName;

    /** 当前累计旷课次数 */
    private Long truantCount;

    /** 预警等级：1轻微 2中等 3严重 */
    private Integer level;

    /** 预警标题 */
    private String title;

    /** 预警内容 */
    private String content;
}

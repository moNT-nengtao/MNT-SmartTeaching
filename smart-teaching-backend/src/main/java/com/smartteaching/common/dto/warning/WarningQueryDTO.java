package com.smartteaching.common.dto.warning;

import lombok.Data;

/**
 * @ClassName WarningQueryDTO
 * @Description
 * @Author MNT
 * @Date 2026/8/31 21:42
 **/
@Data
public class WarningQueryDTO {


    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 预警等级: high/medium/low
     */
    private String level;

    /**
     * 预警类型: absent/score/homework
     */
    private String type;

    /**
     * 学生姓名(模糊搜索)
     */
    private String studentName;

    /**
     * 学号(模糊搜索)
     */
    private String studentNo;

    /**
     * 状态: 1=未处理, 2=已处理
     */
    private Integer status;
}
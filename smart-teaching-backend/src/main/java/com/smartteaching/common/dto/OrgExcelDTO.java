package com.smartteaching.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 组织架构 Excel 导入 DTO
 *
 * Excel 列说明：
 * - 类型：college（学院）/ major（专业）/ class（班级）
 * - 名称：必填
 * - 编码：选填
 * - 父级ID：学院填0，专业填学院ID，班级填专业ID
 * - 年级：仅班级需要
 */
@Data
public class OrgExcelDTO {

    @ExcelProperty(value = "类型", index = 0)
    private String type;  // college / major / class

    @ExcelProperty(value = "名称", index = 1)
    private String name;

    @ExcelProperty(value = "编码", index = 2)
    private String code;  // 选填

    @ExcelProperty(value = "父级ID", index = 3)
    private Long parentId;  // 学院:0, 专业:学院ID, 班级:专业ID

    @ExcelProperty(value = "年级", index = 4)
    private Integer gradeYear;  // 仅班级需要，如 2026
}
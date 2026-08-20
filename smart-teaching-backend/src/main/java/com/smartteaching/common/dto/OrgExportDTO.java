package com.smartteaching.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class OrgExportDTO {

    @ExcelProperty(value = "类型", index = 0)
    private String type;

    @ExcelProperty(value = "名称", index = 1)
    private String name;

    @ExcelProperty(value = "编码", index = 2)
    private String code;

    @ExcelProperty(value = "所属", index = 3)
    private String affiliationName;

    @ExcelProperty(value = "年级", index = 4)
    private Integer gradeYear;
}
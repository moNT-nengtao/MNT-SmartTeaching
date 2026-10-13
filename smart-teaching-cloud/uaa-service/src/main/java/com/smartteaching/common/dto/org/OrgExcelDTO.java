package com.smartteaching.common.dto.org;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName OrgExcelDTO
 * @Description 组织架构Excel导入DTO
 * @Author MNT
 * @Date 2026/8/16 09:52
 **/
@Data
public class OrgExcelDTO {

    @ExcelProperty(value = "类型", index = 0)
    private String type;  // college / major / class

    @ExcelProperty(value = "名称", index = 1)
    private String name;

    @ExcelProperty(value = "编码", index = 2)
    private String code;  // 选填

    @ExcelProperty(value = "所属", index = 3)
    private String affiliationName;

    @ExcelProperty(value = "年级", index = 4)
    private Integer gradeYear;  // 仅班级需要，如 2026
}
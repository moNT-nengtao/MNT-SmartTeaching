package com.smartteaching.common.vo.warning;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName WarningExportVO
 * @Description
 * @Author MNT
 * @Date 2026/8/31 22:02
 **/
@Data
@ColumnWidth(15)
public class WarningExportVO {

    @ExcelProperty(value = "学生姓名", index = 0)
    @ColumnWidth(12)
    private String studentName;

    @ExcelProperty(value = "学号", index = 1)
    @ColumnWidth(16)
    private String studentNo;

    @ExcelProperty(value = "班级", index = 2)
    @ColumnWidth(14)
    private String className;

    @ExcelProperty(value = "预警等级", index = 3)
    @ColumnWidth(12)
    private String levelText;

    @ExcelProperty(value = "预警类型", index = 4)
    @ColumnWidth(14)
    private String typeText;

    @ExcelProperty(value = "预警原因", index = 5)
    @ColumnWidth(40)
    private String reason;

    @ExcelProperty(value = "生成时间", index = 6)
    @ColumnWidth(22)
    private String createTime;

    @ExcelProperty(value = "状态", index = 7)
    @ColumnWidth(12)
    private String statusText;
}
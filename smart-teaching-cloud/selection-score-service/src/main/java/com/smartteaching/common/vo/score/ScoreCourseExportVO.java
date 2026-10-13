package com.smartteaching.common.vo.score;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * @ClassName ScoreCourseExportVO
 * @Description
 * @Author MNT
 * @Date 2026/8/29 01:20
 **/
@Data
public class ScoreCourseExportVO {

    @ExcelProperty("学号")
    @ColumnWidth(20)
    private String studentNo;

    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String studentName;

    @ExcelProperty("班级")
    @ColumnWidth(20)
    private String className;

    @ExcelProperty("平时成绩")
    @ColumnWidth(15)
    private Double usualScore;

    @ExcelProperty("期末成绩")
    @ColumnWidth(15)
    private Double finalScore;

    @ExcelProperty("综合成绩")
    @ColumnWidth(15)
    private Integer totalScore;

    @ExcelProperty("等级")
    @ColumnWidth(12)
    private String level;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String status;

    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;
}
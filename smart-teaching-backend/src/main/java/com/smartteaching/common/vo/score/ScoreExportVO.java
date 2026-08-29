package com.smartteaching.common.vo.score;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ScoreExportVO
 * @Description 异常成绩导出VO
 * @Author MNT
 * @Date 2026/8/28 15:13
 **/
@Data
public class ScoreExportVO {

    @ExcelProperty(value = "学号", index = 0)
    private String studentNo;

    @ExcelProperty(value = "姓名", index = 1)
    private String studentName;

    @ExcelProperty(value = "课程", index = 2)
    private String courseName;

    @ExcelProperty(value = "成绩", index = 3)
    private BigDecimal totalScore;

    @ExcelProperty(value = "异常原因", index = 4)
    private String reason;

    @ExcelProperty(value = "所属学院", index = 5)
    private String collegeName;

    @ExcelProperty(value = "学期", index = 6)
    private String semester;

}

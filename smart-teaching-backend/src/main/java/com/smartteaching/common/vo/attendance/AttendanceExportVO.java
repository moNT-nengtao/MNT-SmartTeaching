package com.smartteaching.common.vo.attendance;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName AttendanceExportVO
 * @Description 考勤报表导出VO
 * @Author MNT
 * @Date 2026/9/2
 **/
@Data
public class AttendanceExportVO {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("考勤状态")
    private String statusText;

    @ExcelProperty("签到时间")
    private String checkinTime;
}

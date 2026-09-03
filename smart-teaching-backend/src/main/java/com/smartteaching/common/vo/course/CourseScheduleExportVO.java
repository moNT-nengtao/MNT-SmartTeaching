package com.smartteaching.common.vo.course;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CourseScheduleExportVO {

    @ExcelIgnore
    private Integer day;

    @ExcelIgnore
    private Integer lesson;

    @ExcelIgnore
    private String weekJson;

    @ExcelProperty(value = "课程", index = 0)
    private String courseName;

    @ExcelProperty(value = "授课教师", index = 1)
    private String teacherName;

    @ExcelProperty(value = "班级", index = 2)
    private String className;

    @ExcelProperty(value = "星期", index = 3)
    private String weekdayText;

    @ExcelProperty(value = "节次", index = 4)
    private String lessonText;

    @ExcelProperty(value = "教室", index = 5)
    private String room;

    @ExcelProperty(value = "周次", index = 6)
    private String week;
}

package com.smartteaching.common.vo.schedule;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ScheduleWeeklyQueryVO
 * @Description
 * @Author MNT
 * @Date 2026/8/30 16:41
 **/
@Data
public class ScheduleWeeklyQueryVO {

    /**
     * 课程列表（扁平结构，前端按 星期+节次 索引渲染）
     */
    private List<CourseItem> courses;

    /**
     * 课表课程项VO
     */
    @Data
    public static class CourseItem {

        /**
         * 排课ID
         */
        private Long id;

        /**
         * 课程名称
         */
        private String courseName;

        /**
         * 授课教师
         */
        private String teacherName;

        /**
         * 教室
         */
        private String classroom;

        /**
         * 课程颜色
         */
        private String color;

        /**
         * 星期几（1-7，周一=1）
         */
        private Integer weekday;

        /**
         * 节次（1-6）
         */
        private Integer lesson;

        /**
         * 上课时间描述（如 "10:00-11:40"）
         */
        private String time;

        /**
         * 周次范围（如 "3-6周"）
         */
        private String weekRange;

        /**
         * 课程备忘
         */
        private String memo;

        /**
         * 周次JSON（仅用于中间处理，不返回给前端）
         */
        private String weekJson;
    }
}
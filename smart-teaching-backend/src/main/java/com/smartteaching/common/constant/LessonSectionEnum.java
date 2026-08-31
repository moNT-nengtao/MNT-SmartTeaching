package com.smartteaching.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName LessonSectionEnum
 * @Description
 * @Author MNT
 * @Date 2026/8/30 17:25
 **/
@Getter
@AllArgsConstructor
public enum LessonSectionEnum {

    SECTION_1(1, "第1-2节", "08:00-09:40"),
    SECTION_2(2, "第3-4节", "10:00-11:40"),
    SECTION_3(3, "第5-6节", "13:30-15:10"),
    SECTION_4(4, "第7-8节", "15:30-17:10"),
    SECTION_5(5, "第9-10节", "18:30-20:10"),
    SECTION_6(6, "第11-12节", "20:30-22:10");

    private final Integer code;
    private final String name;
    private final String timeRange;

    /**
     * 根据节次码获取上课时间描述
     */
    public static String getTimeDesc(Integer code) {
        for (LessonSectionEnum section : values()) {
            if (section.getCode().equals(code)) {
                return section.getTimeRange();
            }
        }
        return "";
    }

    /**
     * 根据节次码获取名称
     */
    public static String getName(Integer code) {
        for (LessonSectionEnum section : values()) {
            if (section.getCode().equals(code)) {
                return section.getName();
            }
        }
        return "";
    }
}
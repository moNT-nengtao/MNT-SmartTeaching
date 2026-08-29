package com.smartteaching.common.vo.selection;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName SelectionStudentVO
 * @Description 课程选课学生名单返回VO
 * @Author MNT
 * @Date 2026/8/22 10:28
 **/
@Data
public class SelectionStudentVO {
    private Long studentId;
    /** 学号 */
    private String studentNo;
    /** 学生姓名 */
    private String studentName;
    /** 年级 */
    private String grade;
    /** 专业名称 */
    private String majorName;
    /** 选课时间 yyyy‑MM‑dd HH:mm:ss */
    private LocalDateTime selectTime;
}
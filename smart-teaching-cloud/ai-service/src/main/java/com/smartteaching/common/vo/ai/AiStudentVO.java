package com.smartteaching.common.vo.ai;

import lombok.Data;

/**
 * @ClassName AiStudentVO
 * @Description 学业分析选择学生时返回的学生信息
 * @Author MNT
 * @Date 2026/9/2 23:15
 **/
@Data
public class AiStudentVO {
    private Long id;
    private String username;
    private String realName;
}

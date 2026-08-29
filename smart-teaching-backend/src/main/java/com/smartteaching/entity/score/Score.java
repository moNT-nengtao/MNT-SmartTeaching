package com.smartteaching.entity.score;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName Score
 * @Description 学生成绩实体类，对应student_score表，存储课程成绩及平时分、期末分等明细
 * @Author MNT
 * @Date 2026/8/14 14:44
 **/
@Data
@TableName("student_score")
public class Score extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long studentId;

    private Long teacherId;

    private BigDecimal score;

    private BigDecimal usualScore;

    private BigDecimal finalScore;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

package com.smartteaching.entity.score;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

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
}

package com.smartteaching.entity.selection;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("selection_record")
public class SelectionRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long courseId;

    private LocalDateTime selectedTime;

    private Integer status;
}

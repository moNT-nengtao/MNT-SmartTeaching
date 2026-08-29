package com.smartteaching.entity.selection;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName SelectionRecord
 * @Description 选课记录实体类，对应selection_record表，记录学生选课操作及选课状态
 * @Author MNT
 * @Date 2026/8/14 07:52
 **/
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

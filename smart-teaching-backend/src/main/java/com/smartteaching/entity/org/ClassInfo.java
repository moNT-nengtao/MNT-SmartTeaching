package com.smartteaching.entity.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName ClassInfo
 * @Description 班级实体类，对应org_class表，管理班级所属专业、年级及学生人数
 * @Author MNT
 * @Date 2026/8/14 14:28
 **/
@Data
@TableName("org_class")
public class ClassInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long majorId;

    private String name;

    private String code;

    private Integer sort;

    private Integer status;

    private Integer gradeYear;

    private Integer studentCount;
}

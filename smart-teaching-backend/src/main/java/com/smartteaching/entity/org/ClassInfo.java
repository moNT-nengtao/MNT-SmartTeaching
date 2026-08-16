package com.smartteaching.entity.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("org_class")
public class ClassInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long majorId;

    private String name;

    private String code;

    private Long parentId;

    private Integer sort;

    private Integer status;

    private Integer gradeYear;
}

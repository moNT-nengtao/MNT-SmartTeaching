package com.smartteaching.entity.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName College
 * @Description 学院实体类，对应org_college表，管理学院基本信息与排序状态
 * @Author MNT
 * @Date 2026/8/14 10:04
 **/
@Data
@TableName("org_college")
public class College extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private Long parentId;

    private Integer sort;

    private Integer status;
}

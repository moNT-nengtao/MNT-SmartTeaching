package com.smartteaching.entity.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName Major
 * @Description 专业实体类，对应org_major表，管理专业所属学院及基本信息
 * @Author MNT
 * @Date 2026/8/14 09:15
 **/
@Data
@TableName("org_major")
public class Major extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeId;

    private String name;

    private String code;

    private Integer sort;

    private Integer status;
}

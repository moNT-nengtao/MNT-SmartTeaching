package com.smartteaching.entity.org;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

@Data
@TableName("org_major")
public class Major extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeId;

    private String name;

    private String code;

    private Long parentId;

    private Integer sort;

    private Integer status;
}

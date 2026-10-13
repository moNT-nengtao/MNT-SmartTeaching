package com.smartteaching.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * @ClassName Role
 * @Description 角色实体类，对应sys_role表，管理系统角色权限标识与描述
 * @Author MNT
 * @Date 2026/8/14 11:06
 **/
@Data
@TableName("sys_role") // 修改为你真实的表名
public class Role implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String description;

}

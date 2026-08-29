package com.smartteaching.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName UserRole
 * @Description 用户角色关联实体类，对应sys_user_role表，管理用户与角色的多对多关系
 * @Author MNT
 * @Date 2026/8/14 13:09
 **/
@Data
@TableName("sys_user_role")
public class UserRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roleId;
}

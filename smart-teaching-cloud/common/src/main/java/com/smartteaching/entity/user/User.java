package com.smartteaching.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName User
 * @Description 用户实体类，对应sys_user表，管理用户认证信息及所属组织关系
 * @Author MNT
 * @Date 2026/8/14 10:41
 **/
@Data
@TableName("sys_user")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private Integer gender;

    private String email;

    private String phone;

    private String avatar;

    private String role;

    private Integer status;

    private Long classId;

    private Long collegeId;

    private Long majorId;

    private LocalDateTime lastLoginTime;

    // 创建时间 - 插入时自动填充
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 更新时间 - 插入和更新时自动填充
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}

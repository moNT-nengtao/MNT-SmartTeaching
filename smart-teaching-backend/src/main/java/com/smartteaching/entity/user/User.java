package com.smartteaching.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

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
}

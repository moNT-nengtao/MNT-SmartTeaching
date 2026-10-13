package com.smartteaching.common.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户返回VO，密码不返回
 */
@Data
public class UserLoginVO {
    private String token; //新增

    private Long id;
    private String username;
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

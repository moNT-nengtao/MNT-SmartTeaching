package com.smartteaching.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表返回前端
 */
@Data
public class UserQueryVO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer status;
    private String phone;
    private String email;
    private Long collegeId;
    private Long classId;

    //联表查询映射字段
    private String collegeName;
    private String className;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

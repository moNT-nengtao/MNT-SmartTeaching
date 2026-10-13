package com.smartteaching.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息内部 VO（跨服务传递用户基础信息）
 * 由 uaa-service 提供，供其它服务 Feign 调用查询用户
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名（登录名） */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色：admin / teacher / student */
    private String role;

    /** 账号状态：1启用 0禁用 */
    private Integer status;
}

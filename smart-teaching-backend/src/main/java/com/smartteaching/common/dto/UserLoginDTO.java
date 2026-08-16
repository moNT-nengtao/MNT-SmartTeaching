package com.smartteaching.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 选择的登录角色 */
    private String role;

    /** 验证码，可选 */
    private String code;

    /** 记住我 */
    private Boolean rememberMe;
}
package com.smartteaching.common.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @ClassName UserLoginDTO
 * @Description 用户登录请求DTO，包含用户名、密码及角色选择等登录参数
 * @Author MNT
 * @Date 2026/8/15 11:46
 **/
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
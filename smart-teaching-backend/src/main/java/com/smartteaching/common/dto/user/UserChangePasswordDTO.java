package com.smartteaching.common.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @ClassName UserChangePasswordDTO
 * @Description 用户修改密码请求DTO，包含旧密码和新密码校验
 * @Author MNT
 * @Date 2026/8/15 09:14
 **/
@Data
public class UserChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

}

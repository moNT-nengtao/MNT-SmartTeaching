package com.smartteaching.common.dto;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserAddDTO {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号仅允许字母数字下划线")
    @Size(min = 3, max = 20, message = "账号长度3‑20字符")
    private String username;

    @Size(min = 6, max = 20, message = "密码6‑20位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名2‑20字符")
    private String realName;

    @NotNull(message = "请选择性别")
    private Integer gender;

    @NotBlank(message = "请选择角色")
    private String role;

    @NotNull(message = "请选择状态")
    private Integer status;

    @NotNull(message = "请选择学院")
    private Long collegeId;

    private Long majorId;

    private Long classId;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Email(message = "邮箱格式错误")
    private String email;
}

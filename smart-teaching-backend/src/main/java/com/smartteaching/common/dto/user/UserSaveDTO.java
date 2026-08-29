package com.smartteaching.common.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * @ClassName UserSaveDTO
 * @Description 用户新增/修改请求DTO，含分组校验
 * @Author MNT
 * @Date 2026/8/15 15:48
 **/
@Data
public class UserSaveDTO {

    public interface AddGroup {}
    public interface UpdateGroup {}

    @NotNull(message = "用户ID不能为空", groups = UpdateGroup.class)
    private Long id;

    @NotBlank(message = "账号不能为空", groups = AddGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "账号仅允许字母数字下划线", groups = AddGroup.class)
    @Size(min = 3, max = 20, message = "账号长度3‑20字符", groups = AddGroup.class)
    private String username;

    @Size(min = 6, max = 20, message = "密码6‑20位", groups = {AddGroup.class, UpdateGroup.class})
    private String password;

    @NotBlank(message = "姓名不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(min = 2, max = 20, message = "姓名2‑20字符", groups = {AddGroup.class, UpdateGroup.class})
    private String realName;

    @NotNull(message = "请选择性别", groups = {AddGroup.class, UpdateGroup.class})
    private Integer gender;

    @NotBlank(message = "请选择角色", groups = {AddGroup.class, UpdateGroup.class})
    private String role;

    @NotNull(message = "请选择状态", groups = {AddGroup.class, UpdateGroup.class})
    private Integer status;

    @NotNull(message = "请选择学院", groups = {AddGroup.class, UpdateGroup.class})
    private Long collegeId;

    private Long majorId;
    private Long classId;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误", groups = {AddGroup.class, UpdateGroup.class})
    private String phone;

    @Email(message = "邮箱格式错误", groups = {AddGroup.class, UpdateGroup.class})
    private String email;
}

package com.smartteaching.common.utils;

import cn.hutool.core.bean.BeanUtil;
import com.smartteaching.common.dto.UserExcelDTO;
import com.smartteaching.entity.user.User;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Excel 导入校验工具类
 */
public class ExcelValidateUtil {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 校验 Excel 导入的用户数据
     */
    public static List<String> validateUser(String username, String realName, String password,
                                            String phone, String email) {
        List<String> errors = new ArrayList<>();

        // 账号校验
        if (username == null || username.trim().isEmpty()) {
            errors.add("账号不能为空");
        } else {
            if (username.length() < 3 || username.length() > 20) {
                errors.add("账号长度3-20字符");
            }
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                errors.add("账号仅字母数字下划线");
            }
        }

        // 密码校验
        if (password == null || password.trim().isEmpty()) {
            errors.add("密码不能为空");
        } else {
            if (password.length() < 6 || password.length() > 20) {
                errors.add("密码长度6-20位");
            }
        }

        // 姓名校验
        if (realName == null || realName.trim().isEmpty()) {
            errors.add("姓名不能为空");
        }

        // 手机号校验
        if (phone != null && !phone.isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                errors.add("手机号格式错误");
            }
        }

        // 邮箱校验
        if (email != null && !email.isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("邮箱格式错误");
            }
        }

        return errors;
    }

    /**
     * 格式化错误信息
     */
    public static String formatError(int rowNum, String username, List<String> errors) {
        String displayName = username != null && !username.isEmpty() ? username : "空账号";
        return String.format("第 %d 行 (%s) 错误: %s", rowNum, displayName, String.join("；", errors));
    }

    /**
     * 构建 User 对象
     */
    public static User buildUser(UserExcelDTO data, String defaultAvatar) {
        User user = new User();
        BeanUtil.copyProperties(data, user, "password");

        user.setPassword(DigestUtils.md5DigestAsHex(data.getPassword().getBytes()));
        user.setAvatar(defaultAvatar);
        user.setStatus(1);

        // 性别转换
        if ("男".equals(data.getGender())) {
            user.setGender(1);
        } else if ("女".equals(data.getGender())) {
            user.setGender(2);
        } else {
            user.setGender(0);
        }

        String role = data.getRole();
        user.setRole(role == null || role.isEmpty() ? "student" : role);

        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        return user;
    }
}
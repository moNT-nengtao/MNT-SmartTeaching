package com.smartteaching.service.auth;

import com.smartteaching.common.dto.UserChangePasswordDTO;
import com.smartteaching.common.dto.UserLoginDTO;
import com.smartteaching.entity.user.User;

public interface AuthService {
    /**
     * 登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 获取当前登录用户信息
     * @param username
     * @return
     */
    User getUserByUsername(String username);

    /**
     * 效验旧密码
     */
    void oldPasswordVerification(UserChangePasswordDTO userChangePasswordDTO);
}

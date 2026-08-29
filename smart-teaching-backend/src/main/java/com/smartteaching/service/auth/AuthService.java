package com.smartteaching.service.auth;

import com.smartteaching.common.dto.user.UserChangePasswordDTO;
import com.smartteaching.common.dto.user.UserLoginDTO;
import com.smartteaching.entity.user.User;

/**
 * @ClassName AuthService
 * @Description 认证服务接口
 * @Author MNT
 * @Date 2026/8/15 11:15
 **/
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

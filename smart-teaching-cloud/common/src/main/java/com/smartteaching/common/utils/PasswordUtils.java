package com.smartteaching.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类 BCrypt
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 密码加密
     * @param rawPassword 明文密码
     * @return 加密后的密文
     */
    public static String encrypt(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 密码校验
     * @param rawPassword 前端传入明文密码
     * @param encodedPassword 数据库存储加密密码
     * @return true密码匹配；false密码错误
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

}

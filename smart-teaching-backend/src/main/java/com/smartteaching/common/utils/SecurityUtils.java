package com.smartteaching.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @ClassName SecurityUtils
 * @Description 安全工具类：获取当前登录用户信息
 * @Author MNT
 * @Date 2026/8/31 23:30
 **/
public class SecurityUtils {

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        // 匿名用户返回 null
        if ("anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isAuthenticated() {
        return getCurrentUsername() != null;
    }
}

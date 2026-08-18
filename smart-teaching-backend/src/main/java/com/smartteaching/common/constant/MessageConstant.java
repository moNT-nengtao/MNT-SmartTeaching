package com.smartteaching.common.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    // ========== 用户认证相关 ==========
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在或已禁用";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ROLE_MISMATCH = "账号不存在或已禁用";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String ALREADY_EXISTS = "已存在";

    // ========== 通用错误 ==========
    public static final String UNKNOWN_ERROR = "未知错误";

    // ========== 文件/头像相关 ==========
    public static final String AVATAR_SAVE_IO_ERROR = "头像文件保存失败，请检查磁盘空间或权限";
    public static final String AVATAR_DIR_CREATE_FAILED = "头像存储目录创建失败";

    // ========== 用户管理相关 ==========
    public static final String ADD_USER_AVATAR_FAIL_ROLLBACK = "头像保存失败，已回滚新增用户数据";
}

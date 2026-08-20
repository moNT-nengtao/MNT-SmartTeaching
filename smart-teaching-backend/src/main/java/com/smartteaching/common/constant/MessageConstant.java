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
    public static final String AVATAR_WRITE_SUCCESS = "新头像磁盘写入成功";
    public static final String AVATAR_SAVE_IO_ERROR = "头像文件保存失败，请检查磁盘空间或权限";
    public static final String AVATAR_DIR_CREATE_FAILED = "头像存储目录创建失败";

    // ========== 用户管理相关 ==========
    public static final String ADD_USER_AVATAR_FAIL_ROLLBACK = "头像保存失败，已回滚新增用户数据";
    public static final String DELETE_USER_FAIL = "删除用户失败";
    public static final String CANNOT_DELETE_SELF = "不能删除当前登录账号";
    public static final String CANNOT_DELETE_ADMIN = "不允许删除管理员账号";
    public static final String CANNOT_DISABLE_ADMIN = "不允许禁用管理员账号";
    public static final String CANNOT_DISABLE_SELF = "不能禁用当前登录账号";
    public static final String ACCOUNT_DISABLED = "账号已被禁用";
    public static final String OPERATE_USER_NOT_EXIST = "操作用户已不存在";


    // ========== Excel批量导入用户 常量【新增】 ==========
    /** 读取Excel文件失败 */
    public static final String EXCEL_READ_IO_FAIL = "读取 Excel 文件失败";
    /** Excel导入存在校验错误模板 */
    public static final String EXCEL_IMPORT_VALID_ERROR_TPL = "导入失败，共 %d 条错误：%s";
    /** 账号已存在导入失败 */
    public static final String EXCEL_IMPORT_ACCOUNT_EXIST = "账号 %s 已存在，导入失败";
    /** 通用导入失败 */
    public static final String EXCEL_IMPORT_COMMON_FAIL = "导入失败: %s";

}

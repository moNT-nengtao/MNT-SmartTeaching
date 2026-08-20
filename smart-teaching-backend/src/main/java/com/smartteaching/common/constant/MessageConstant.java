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


    // ========== Excel批量导入用户  ==========
    public static final String EXCEL_READ_IO_FAIL = "读取 Excel 文件失败";
    public static final String EXCEL_IMPORT_VALID_ERROR_TPL = "导入失败，共 %d 条错误：%s";
    public static final String EXCEL_FILE_EMPTY = "Excel 文件为空或没有有效数据";
    public static final String NO_VALID_DATA_IMPORT = "没有有效数据可导入";

    // ========== 组织节点 ==========
    public static final String COLLEGE_HAS_MAJOR = "该学院下存在专业，禁止删除，请先删除下属专业";
    public static final String MAJOR_HAS_CLASS = "该专业下存在班级，禁止删除，请先删除下属班级";
    public static final String CLASS_HAS_STUDENT = "该班级下存在学生，禁止删除，请先移除该班级下的学生";
    public static final String ORG_TYPE_ILLEGAL = "非法节点类型 ";
    public static final String ADD_ORG_NODE_FAIL = "新增组织节点失败";
    public static final String EDIT_ORG_NODE_FAIL = "编辑组织节点失败";

    public static final String ORG_NODE_NOT_EXIST = "该组织节点不存在";
    public static final String CLASS_GRADE_YEAR_NOT_NULL = "班级年级不能为空";
    public static final String CLASS_NOT_EXIST = "班级不存在";
    public static final String COLLEGE_NOT_EXIST_OR_DISABLED = "所属学院不存在或已禁用";
    public static final String MAJOR_NOT_EXIST = "专业不存在";
    public static final String COLLEGE_NOT_EXIST = "学院不存在";
    public static final String REVERT_CLASS_FAIL = "恢复班级失败";
    public static final String MAJOR_NOT_EXIST_OR_DISABLED = "所属专业不存在或已禁用";
    public static final String REVERT_MAJOR_FAIL = "恢复专业失败";
    public static final String REVERT_COLLEGE_FAIL = "恢复学院失败";
}

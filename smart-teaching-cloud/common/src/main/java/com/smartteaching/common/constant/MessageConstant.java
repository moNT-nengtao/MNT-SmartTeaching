package com.smartteaching.common.constant;

/**
 * @ClassName MessageConstant
 * @Description 信息常量类
 * @Author MNT
 * @Date 2026/8/14 14:37
 **/
public class MessageConstant {

    // ========== 用户认证相关 ==========
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在或已禁用";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ROLE_MISMATCH = "账号不存在或已禁用";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String USER_ILLEGAL = "非法用户操作";
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

    // ========== Excel批量导入用户 ==========
    public static final String EXCEL_READ_IO_FAIL = "读取 Excel 文件失败";
    public static final String EXCEL_IMPORT_VALID_ERROR_TPL = "导入失败，共 %d 条错误：%s";
    public static final String EXCEL_FILE_EMPTY = "Excel 文件为空或没有有效数据";
    public static final String NO_VALID_DATA_IMPORT = "没有有效数据可导入";

    // ========== 组织节点 ==========
    // -- 学院 --
    public static final String COLLEGE_NOT_EXIST = "学院不存在";
    public static final String COLLEGE_NOT_EXIST_OR_DISABLED = "所属学院不存在或已禁用";
    public static final String COLLEGE_HAS_MAJOR = "该学院下存在专业，禁止删除，请先删除下属专业";
    public static final String REVERT_COLLEGE_FAIL = "恢复学院失败";
    public static final String DISABLED_COLLEGE_EXISTS = "存在已禁用的同名学院：%s，请先恢复或修改名称";

    // -- 专业 --
    public static final String MAJOR_NOT_EXIST = "专业不存在";
    public static final String MAJOR_NOT_EXIST_OR_DISABLED = "所属专业不存在或已禁用";
    public static final String MAJOR_HAS_CLASS = "该专业下存在班级，禁止删除，请先删除下属班级";
    public static final String REVERT_MAJOR_FAIL = "恢复专业失败";
    public static final String DISABLED_MAJOR_EXISTS = "存在已禁用的同名专业：%s，请先恢复或修改名称";

    // -- 班级 --
    public static final String CLASS_NOT_EXIST = "班级不存在";
    public static final String CLASS_HAS_STUDENT = "该班级下存在学生，禁止删除，请先移除该班级下的学生";
    public static final String CLASS_GRADE_YEAR_NOT_NULL = "班级年级不能为空";
    public static final String REVERT_CLASS_FAIL = "恢复班级失败";
    public static final String DISABLED_CLASS_EXISTS = "存在已禁用的同名班级：%s，请先恢复或修改名称";

    // -- 通用 --
    public static final String ORG_TYPE_ILLEGAL = "非法节点类型 ";
    public static final String ADD_ORG_NODE_FAIL = "新增组织节点失败";
    public static final String EDIT_ORG_NODE_FAIL = "编辑组织节点失败";
    public static final String ORG_NODE_NOT_EXIST = "该组织节点不存在";

    // ========== 课程管理 ==========
    public static final String COURSE_NOT_EXIST = "该课程已不存在";
    public static final String COURSE_CODE_EXISTS = "已经存在编号：%s";

    // ========== 排课管理 ==========
    public static final String CONFLICT_BATCH_TEACHER = "与批量导入项冲突（教师）";
    public static final String CONFLICT_BATCH_CLASS = "与批量导入项冲突（班级）";
    public static final String CONFLICT_BATCH_ROOM = "与批量导入项冲突（教室）";
    public static final String SCHEDULE_CONFLICT = "存在排课冲突，请检查";
    public static final String SCHEDULE_CONFLICT_DESC = "与数据库中现有排课冲突";
    public static final String SCHEDULE_NOT_EXIST = "排课记录不存在";

    // ========== 成绩统计 ==========
    public static final String SCORE_EXCELLENT = "优秀(≥90)";
    public static final String SCORE_GOOD = "良好(80‑89)";
    public static final String SCORE_MEDIUM = "中等(70‑79)";
    public static final String SCORE_PASS = "及格(60‑69)";
    public static final String SCORE_FAIL = "不及格(<60)";

    // ========== 推荐系统 ==========
    public static final String RECOMMEND_REASON_TEACHER = "您选择过的教师";
    public static final String RECOMMEND_REASON_MAJOR = "与您的专业相关";
    public static final String RECOMMEND_REASON_HOT_COURSE = "热门课程推荐";
    public static final String RECOMMEND_REASON_HIGH_SCORE_COURSE = "高分课程推荐";

    // ========== 预警模块 ==========
    public static final String WARNING_NOT_EXIST = "预警记录不存在";
    public static final String WARNING_LEVEL_SEVERE = "严重";
    public static final String WARNING_LEVEL_MEDIUM = "中等";
    public static final String WARNING_LEVEL_SLIGHT = "轻微";
    public static final String WARNING_TYPE_ABSENT = "旷课预警";
    public static final String WARNING_TYPE_SCORE = "挂科预警";
    public static final String WARNING_TYPE_HOMEWORK = "作业未提交预警";
    public static final String WARNING_STATUS_HANDLED = "已处理";
    public static final String WARNING_STATUS_UNHANDLED = "未处理";

    public static final String SUGGEST_HIGH_1 = "立即联系辅导员和任课教师，制定紧急补习计划";
    public static final String SUGGEST_HIGH_2 = "每日安排至少2小时额外学习时间，重点攻克薄弱科目";
    public static final String SUGGEST_MEDIUM_1 = "主动与任课教师沟通，了解课程重点和提升方法";
    public static final String SUGGEST_MEDIUM_2 = "制定每周学习计划，合理分配各科学习时间";
    public static final String SUGGEST_LOW_1 = "保持现有学习状态，重点关注薄弱知识点";
    public static final String SUGGEST_LOW_2 = "定期进行自我评估，及时调整学习策略";
    public static final String SUGGEST_SCORE_1 = "重点复习不及格课程，争取补考通过";
    public static final String SUGGEST_SCORE_2 = "整理错题本，针对性强化训练";
    public static final String SUGGEST_ABSENT_1 = "严格遵守考勤制度，确保全勤";
    public static final String SUGGEST_ABSENT_2 = "如有特殊情况及时请假，避免无故缺勤";
    public static final String SUGGEST_HOMEWORK_1 = "按时完成并提交作业，避免影响平时成绩";
    public static final String SUGGEST_HOMEWORK_2 = "合理规划时间，避免作业堆积";

    // ========== 考勤模块 ==========
    public static final String ATTENDANCE_COURSE_NOT_EXIST = "课程不存在或已被禁用";
    public static final String ATTENDANCE_COURSE_NOT_OWNED = "只能为自己授课的课程发起签到";
    public static final String ATTENDANCE_NO_STUDENTS = "该课程暂无有效选课学生，无法发起签到";
    public static final String ATTENDANCE_PATTERN_INVALID = "签到图案无效，请至少连接3个节点";
    public static final String ATTENDANCE_DURATION_INVALID = "签到时长必须在1-20分钟之间";
    public static final String ATTENDANCE_SESSION_NOT_EXIST = "签到会话不存在";
    public static final String ATTENDANCE_SESSION_EXPIRED = "签到会话已结束或已过期";
    public static final String ATTENDANCE_PATTERN_MISMATCH = "签到图案不正确，请核对后重试";
    public static final String ATTENDANCE_ALREADY_CHECKED = "您已完成本次签到，无需重复签到";
    public static final String ATTENDANCE_MARKED_TPL = "您已被标记为%s，无需签到";
    public static final String ATTENDANCE_NOT_STUDENT = "只有学生可以进行签到";
    public static final String ATTENDANCE_NOT_TEACHER = "只有教师可以操作签到会话";
    public static final String ATTENDANCE_NO_PERMISSION = "无权操作该签到会话";
    public static final String ATTENDANCE_RECORD_NOT_EXIST = "考勤记录不存在";
    public static final String ATTENDANCE_STATUS_INVALID = "教师只能将考勤状态修改为迟到、请假或旷课";
    public static final String ATTENDANCE_ALREADY_ENDED = "签到会话已结束";
    public static final String ATTENDANCE_SESSION_ENDED = "签到会话已结束，历史考勤不可修改";
    public static final String ATTENDANCE_MANUAL_CHECKIN_FAILED = "该学生当前状态无法手动签到";
}

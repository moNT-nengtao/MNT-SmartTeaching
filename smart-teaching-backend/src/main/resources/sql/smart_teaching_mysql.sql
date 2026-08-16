DROP DATABASE IF EXISTS smart_teaching;
CREATE DATABASE smart_teaching CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_teaching;

-- =====================================================
-- 1. 用户与权限模块
-- =====================================================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名/工号',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    gender TINYINT DEFAULT 0 COMMENT '0=未知,1=男,2=女',
    email VARCHAR(128) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    role VARCHAR(32) NOT NULL COMMENT 'admin/teacher/student',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用,1=启用',
    class_id BIGINT DEFAULT NULL COMMENT '班级ID',
    college_id BIGINT DEFAULT NULL COMMENT '学院ID',
    major_id BIGINT DEFAULT NULL COMMENT '专业ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_time DATETIME DEFAULT NULL,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_class_id (class_id)
) COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='角色表';

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT='用户角色关联表';

-- =====================================================
-- 2. 组织架构模块
-- =====================================================
CREATE TABLE org_college (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '学院名称',
    code VARCHAR(64) DEFAULT NULL COMMENT '学院编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父节点，顶层为0',
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='学院表';

CREATE TABLE org_major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    college_id BIGINT NOT NULL COMMENT '所属学院',
    name VARCHAR(128) NOT NULL COMMENT '专业名称',
    code VARCHAR(64) DEFAULT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_college_id (college_id)
) COMMENT='专业表';

CREATE TABLE org_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL COMMENT '所属专业',
    name VARCHAR(128) NOT NULL COMMENT '班级名称',
    code VARCHAR(64) DEFAULT NULL,
    parent_id BIGINT DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    grade_year INT DEFAULT NULL COMMENT '年级',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_id (major_id)
) COMMENT='班级表';

-- =====================================================
-- 3. 课程与排课模块
-- =====================================================
CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE COMMENT '课程编号',
    name VARCHAR(128) NOT NULL COMMENT '课程名称',
    teacher_id BIGINT NOT NULL COMMENT '任课教师',
    credit DECIMAL(4,2) NOT NULL DEFAULT 0.00 COMMENT '学分',
    semester VARCHAR(32) DEFAULT NULL COMMENT '学期',
    capacity INT NOT NULL DEFAULT 0 COMMENT '总容量',
    description TEXT DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=停用,1=启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_semester (semester)
) COMMENT='课程表';

CREATE TABLE course_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    week INT NOT NULL COMMENT '周次',
    day INT NOT NULL COMMENT '星期1-7',
    lesson INT NOT NULL COMMENT '第几节',
    room VARCHAR(64) DEFAULT NULL COMMENT '教室',
    color VARCHAR(32) DEFAULT NULL COMMENT '课程颜色',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常,0=删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_schedule (course_id, week, day, lesson, room),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_class_id (class_id),
    INDEX idx_course_id (course_id)
) COMMENT='排课表';

-- =====================================================
-- 4. 选课模块
-- =====================================================
CREATE TABLE selection_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    start_time DATETIME NOT NULL COMMENT '选课开始时间',
    end_time DATETIME NOT NULL COMMENT '选课结束时间',
    min_credit DECIMAL(4,2) DEFAULT 1.00 COMMENT '最低选课学分',
    max_credit DECIMAL(4,2) DEFAULT 6.00 COMMENT '最高选课学分',
    allowed_majors TEXT DEFAULT NULL COMMENT '允许专业列表JSON',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=关闭,1=开启',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='选课配置表';

CREATE TABLE selection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    selected_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已选,0=退选',
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    UNIQUE KEY uk_student_course (student_id, course_id)
) COMMENT='学生选课记录表';

-- =====================================================
-- 5. 成绩模块
-- =====================================================
CREATE TABLE student_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    score DECIMAL(5,2) DEFAULT NULL COMMENT '成绩',
    usual_score DECIMAL(5,2) DEFAULT NULL COMMENT '平时成绩',
    final_score DECIMAL(5,2) DEFAULT NULL COMMENT '期末成绩',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=有效,0=删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_student (course_id, student_id),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id)
) COMMENT='学生成绩表';

-- =====================================================
-- 6. 考勤模块
-- =====================================================
CREATE TABLE attendance_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME DEFAULT NULL,
    end_time TIME DEFAULT NULL,
    check_code VARCHAR(16) NOT NULL COMMENT '签到码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=进行中,0=已结束',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_class_id (class_id)
) COMMENT='签到会话表';

CREATE TABLE attendance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    longitude DECIMAL(10,6) DEFAULT NULL,
    latitude DECIMAL(10,6) DEFAULT NULL,
    checkin_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已签到,0=缺勤',
    INDEX idx_session_id (session_id),
    INDEX idx_student_id (student_id)
) COMMENT='签到记录表';

-- =====================================================
-- 7. 公告模块
-- =====================================================
CREATE TABLE notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT NOT NULL,
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '0=不置顶,1=置顶',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已发布,0=撤回',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_status (status)
) COMMENT='公告表';

CREATE TABLE notice_read_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notice_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    INDEX idx_notice_id (notice_id),
    INDEX idx_user_id (user_id)
) COMMENT='公告已读记录表';

-- =====================================================
-- 8. 问答模块
-- =====================================================
CREATE TABLE qa_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    tags VARCHAR(255) DEFAULT NULL COMMENT '标签逗号分隔',
    is_top TINYINT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT='问答问题表';

CREATE TABLE qa_reply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_question_id (question_id),
    INDEX idx_user_id (user_id)
) COMMENT='问答回复表';

-- =====================================================
-- 9. AI 助教模块
-- =====================================================
CREATE TABLE ai_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) DEFAULT NULL,
    model_name VARCHAR(64) DEFAULT 'gpt-4o-mini',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT='AI会话表';

CREATE TABLE ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    sender TINYINT NOT NULL COMMENT '0=用户,1=AI',
    content TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) COMMENT='AI消息记录表';

-- =====================================================
-- 10. 评价模块
-- =====================================================
CREATE TABLE course_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score DECIMAL(3,2) NOT NULL COMMENT '总体评分',
    content TEXT DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_student_id (student_id)
) COMMENT='课程评价表';

-- =====================================================
-- 11. 学业预警模块
-- =====================================================
CREATE TABLE warning_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '学生ID',
    warning_type VARCHAR(32) NOT NULL COMMENT '预警类型',
    level TINYINT NOT NULL COMMENT '1=低,2=中,3=高',
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=未处理,2=已处理',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_warning_type (warning_type)
) COMMENT='预警记录表';

-- =====================================================
-- 12. 仪表盘统计模块（可视化数据表）
-- =====================================================
CREATE TABLE dashboard_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_type VARCHAR(64) NOT NULL COMMENT '统计类型',
    target_date DATE NOT NULL,
    target_id BIGINT DEFAULT NULL,
    value JSON DEFAULT NULL COMMENT '统计数据JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stat_type (stat_type),
    INDEX idx_target_date (target_date)
) COMMENT='仪表盘统计缓存表';

-- =====================================================
-- 13. 插入基础角色数据
-- =====================================================
INSERT INTO sys_role (code, name, description) VALUES
('admin', '管理员', '平台管理员'),
('teacher', '教师', '任课教师'),
('student', '学生', '学生用户');



-- =====================================================
-- Redis 建议说明（不放在 MySQL 中）
-- =====================================================
-- 1. JWT Token 黑名单与刷新 token：避免单点登录失效后继续使用旧 token。
-- 2. 登录验证码、短信验证码、邮箱验证码：高频临时数据，TTL 短，易失效。
-- 3. 选课热门缓存与推荐课程缓存：减少重复计算和高频查询。
-- 4. 课程表、考勤统计、仪表盘数据缓存：适合缓存，避免重复汇总查询。
-- 5. 公告未读数缓存：用于高并发场景下快速展示。
-- 6. AI 会话上下文与历史摘要：避免重复模型调用，节省成本。
-- 7. 签到二维码临时状态：动态码和过期时间最好由 Redis 控制。
-- 8. 分布式锁/限流 key：用于防止重复提交、库存扣减和高并发恶意请求。
-- 9. 用户权限/角色权限缓存：提升登录后权限校验速度。
-- 10. 高频可重建数据：如首页统计、热榜、最近公告等，适合放 Redis 缓存。
--
-- 说明：MySQL 主要存放“真实业务数据、主数据、事务数据、永久记录”，
-- Redis 负责“高频缓存、临时状态、会话/验证码、限流与热点数据”。

-- 结束

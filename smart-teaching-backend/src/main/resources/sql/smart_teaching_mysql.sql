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
                           college_id BIGINT NOT NULL COMMENT '所属学院ID',
                           name VARCHAR(128) NOT NULL COMMENT '专业名称',
                           code VARCHAR(64) DEFAULT NULL,
                           sort INT NOT NULL DEFAULT 0,
                           status TINYINT NOT NULL DEFAULT 1,
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           INDEX idx_college_id (college_id)
) COMMENT='专业表';

CREATE TABLE org_class (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           major_id BIGINT NOT NULL COMMENT '所属专业ID',
                           name VARCHAR(128) NOT NULL COMMENT '班级名称',
                           code VARCHAR(64) DEFAULT NULL,
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
                        teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
                        credit DECIMAL(4,2) NOT NULL DEFAULT 0.00 COMMENT '学分',
                        semester VARCHAR(32) DEFAULT NULL COMMENT '学期',
                        capacity INT NOT NULL DEFAULT 0 COMMENT '选课容量',
                        description TEXT DEFAULT NULL,
                        status TINYINT NOT NULL DEFAULT 1 COMMENT '0=停用,1=启用',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        INDEX idx_teacher_id (teacher_id),
                        INDEX idx_semester (semester)
) COMMENT='课程表';

-- ⚠️ JSON类型不能参与UNIQUE KEY，因此删除原 uk_schedule 唯一约束；其余索引全部原样保留
CREATE TABLE course_schedule (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 course_id BIGINT NOT NULL,
                                 teacher_id BIGINT NOT NULL,
                                 class_id BIGINT NOT NULL,
                                 week JSON NOT NULL COMMENT '周次数组，例：[1,2,3,4,9,10,11,12]',
                                 day INT NOT NULL COMMENT '星期1‑7',
                                 lesson INT NOT NULL COMMENT '第几节课',
                                 room VARCHAR(64) DEFAULT NULL COMMENT '教室',
                                 color VARCHAR(32) DEFAULT NULL COMMENT '日历颜色',
                                 status TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常,0=删除',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
                                  scope_type VARCHAR(20) DEFAULT 'all' COMMENT '选课范围类型: all/grade/major',
                                  scope_value TEXT DEFAULT NULL COMMENT '范围值JSON数组: ["2023","2024"] 或 [1,2,3]',
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
                               score DECIMAL(5,2) DEFAULT NULL COMMENT '总评成绩',
                               usual_score DECIMAL(5,2) DEFAULT NULL COMMENT '平时成绩',
                               final_score DECIMAL(5,2) DEFAULT NULL COMMENT '期末成绩',
                               remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
                               status TINYINT NOT NULL DEFAULT 1 COMMENT '1=有效,0=作废',
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
                                    class_id BIGINT DEFAULT NULL COMMENT '班级ID（课程跨班级时为空，学生以选课记录为准）',
                                    session_date DATE NOT NULL COMMENT '上课日期',
                                    start_time TIME DEFAULT NULL,
                                    end_time TIME DEFAULT NULL,
                                    check_code VARCHAR(16) NOT NULL COMMENT '签到码/九宫格图案序列(如0,1,4,7)',
                                    duration INT DEFAULT NULL COMMENT '签到有效时长（分钟，上限20）',
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
                                   checkin_time DATETIME DEFAULT NULL COMMENT '签到时间（未签到为空）',
                                   status TINYINT NOT NULL DEFAULT 0 COMMENT '0=缺勤,1=考勤成功,2=迟到,3=请假,4=旷课,5=手动签到(教师代签,特殊留痕)',
                                   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   UNIQUE KEY uk_session_student (session_id, student_id),
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
                        type VARCHAR(20) NOT NULL DEFAULT 'system' COMMENT '公告类型:system=全校,course=课程,notice=普通通知',
                        course_id BIGINT DEFAULT NULL COMMENT '关联课程ID（课程公告）',
                        publisher_id BIGINT NOT NULL,
                        publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        is_top TINYINT NOT NULL DEFAULT 0 COMMENT '0=不置顶,1=置顶',
                        status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已发布,0=撤回',
                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        INDEX idx_publisher_id (publisher_id),
                        INDEX idx_status (status),
                        INDEX idx_type (type),
                        INDEX idx_course_id (course_id)
) COMMENT='公告表';

CREATE TABLE notice_read_record (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    notice_id BIGINT NOT NULL,
                                    user_id BIGINT NOT NULL,
                                    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE KEY uk_notice_user (notice_id, user_id),
                                    INDEX idx_notice_id (notice_id),
                                    INDEX idx_user_id (user_id)
) COMMENT='公告阅读记录表';

-- =====================================================
-- 8. 问答模块
-- =====================================================
CREATE TABLE qa_question (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id BIGINT NOT NULL,
                             course_id BIGINT DEFAULT NULL COMMENT '关联课程ID（课程分区）',
                             title VARCHAR(255) NOT NULL,
                             content TEXT NOT NULL,
                             tags VARCHAR(255) DEFAULT NULL COMMENT '标签逗号分隔',
                             is_anonymous TINYINT NOT NULL DEFAULT 0 COMMENT '0=实名,1=匿名',
                             is_top TINYINT NOT NULL DEFAULT 0,
                             like_count INT NOT NULL DEFAULT 0,
                             reply_count INT NOT NULL DEFAULT 0,
                             status TINYINT NOT NULL DEFAULT 1,
                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             INDEX idx_user_id (user_id),
                             INDEX idx_status (status),
                             INDEX idx_course_id (course_id)
) COMMENT='问题表';

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
) COMMENT='回答表';

-- =====================================================
-- 9. AI助教模块
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
                            sender TINYINT NOT NULL COMMENT '0用户，1AI',
                            content TEXT NOT NULL,
                            create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_session_id (session_id)
) COMMENT='AI消息表';

-- AI每日调用统计表：按用户+日期统计使用次数，实现每日问答次数限制
CREATE TABLE ai_daily_usage (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL COMMENT '用户ID',
                                use_date DATE NOT NULL COMMENT '使用日期',
                                use_count INT NOT NULL DEFAULT 0 COMMENT '当日已用次数',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                UNIQUE KEY uk_user_date (user_id, use_date)
) COMMENT='AI每日调用统计表';

-- =====================================================
-- 10. 课程评价模块（含各维度打分）
-- =====================================================
DROP TABLE IF EXISTS course_evaluation;
CREATE TABLE course_evaluation (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   course_id BIGINT NOT NULL COMMENT '课程ID',
                                   teacher_id BIGINT NOT NULL COMMENT '教师ID',
                                   student_id BIGINT NOT NULL COMMENT '学生ID',
                                   score DECIMAL(3,2) NOT NULL COMMENT '综合评分（1-5分）',
    -- 各维度打分（1-5分）
                                   teaching_ability DECIMAL(3,2) DEFAULT NULL COMMENT '授课能力评分',
                                   class_atmosphere DECIMAL(3,2) DEFAULT NULL COMMENT '课堂氛围评分',
                                   knowledge_clarity DECIMAL(3,2) DEFAULT NULL COMMENT '知识讲解清晰度评分',
                                   homework_feedback DECIMAL(3,2) DEFAULT NULL COMMENT '作业批改反馈评分',
                                   qa_service DECIMAL(3,2) DEFAULT NULL COMMENT '答疑服务评分',

                                   content TEXT DEFAULT NULL COMMENT '评价内容',
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
                                level TINYINT NOT NULL COMMENT '1低，2中，3高',
                                title VARCHAR(255) NOT NULL,
                                content TEXT NOT NULL,
                                status TINYINT NOT NULL DEFAULT 1 COMMENT '1未处理，2已处理',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                INDEX idx_user_id (user_id),
                                INDEX idx_warning_type (warning_type)
) COMMENT='学业预警记录表';

-- =====================================================
-- 12. 仪表盘统计缓存表
-- =====================================================
CREATE TABLE dashboard_stat (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                stat_type VARCHAR(64) NOT NULL COMMENT '统计类型',
                                target_date DATE NOT NULL,
                                target_id BIGINT DEFAULT NULL,
                                value JSON DEFAULT NULL COMMENT '统计JSON数据',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                INDEX idx_stat_type (stat_type),
                                INDEX idx_target_date (target_date)
) COMMENT='仪表盘统计缓存表';

-- =====================================================
-- 13. 作业表
-- =====================================================
CREATE TABLE IF NOT EXISTS homework (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        course_id BIGINT NOT NULL COMMENT '课程ID',
                                        teacher_id BIGINT NOT NULL COMMENT '发布教师ID',
                                        title VARCHAR(255) NOT NULL COMMENT '作业标题',
                                        content TEXT COMMENT '作业要求/内容',
                                        attachment_url VARCHAR(500) DEFAULT NULL COMMENT '附件URL（可选）',
                                        attachment_name VARCHAR(255) DEFAULT NULL COMMENT '附件原始文件名',
                                        deadline DATETIME DEFAULT NULL COMMENT '截止时间',
                                        status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已发布,0=已撤销',
                                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        INDEX idx_course_id (course_id),
                                        INDEX idx_teacher_id (teacher_id)
) COMMENT='作业表';


-- =====================================================
-- 14. 作业提交表
-- =====================================================
CREATE TABLE IF NOT EXISTS homework_submission (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                   homework_id BIGINT NOT NULL COMMENT '作业ID',
                                                   student_id BIGINT NOT NULL COMMENT '学生ID',
                                                   content TEXT COMMENT '提交内容',
                                                   attachment_url VARCHAR(500) DEFAULT NULL COMMENT '提交附件URL',
                                                   attachment_name VARCHAR(255) DEFAULT NULL COMMENT '附件原始文件名',
                                                   score DECIMAL(5,2) DEFAULT NULL COMMENT '成绩（NULL=未批改）',
                                                   comment VARCHAR(500) DEFAULT NULL COMMENT '教师评语',
                                                   submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   grade_time DATETIME DEFAULT NULL COMMENT '批改时间',
                                                   status TINYINT NOT NULL DEFAULT 1 COMMENT '1=已提交,0=已撤销',
                                                   UNIQUE KEY uk_homework_student (homework_id, student_id),
                                                   INDEX idx_homework_id (homework_id),
                                                   INDEX idx_student_id (student_id)
) COMMENT='作业提交表';


-- =====================================================
-- 初始化基础数据 + 扩充测试数据
-- =====================================================
INSERT INTO sys_role(id, code, name, description) VALUES
                                                      (1, 'admin', '管理员', '平台管理员'),
                                                      (2, 'teacher', '教师', '任课教师'),
                                                      (3, 'student', '学生', '学生用户');

-- 扩充学院
INSERT INTO org_college(id, name, code, parent_id, sort, status) VALUES
                                                                     (1, '计算机科学与技术学院', 'CS01', 0, 1, 1),
                                                                     (2, '外国语学院', 'FL01', 0, 2, 1),
                                                                     (3, '经济管理学院', 'EM01', 0, 3, 1),
                                                                     (4, '机电工程学院', 'ME01', 0, 4, 1),
                                                                     (5, '数理学院', 'MS01', 0, 5, 1);

-- 扩充专业
INSERT INTO org_major(id, college_id, name, code, sort, status) VALUES
                                                                    (1, 1, '计算机科学与技术', 'CS-2023', 1, 1),
                                                                    (2, 1, '软件工程', 'SE-2023', 2, 1),
                                                                    (3, 2, '英语', 'ENG-2023', 1, 1),
                                                                    (4, 3, '工商管理', 'MBA-2023', 1, 1),
                                                                    (5, 4, '机械设计制造', 'ME-2023', 1, 1),
                                                                    (6, 5, '应用数学', 'MATH-2023', 1, 1);

-- 扩充班级
INSERT INTO org_class(id, major_id, name, code, sort, status, grade_year) VALUES
                                                                              (1, 1, '计科2301班', 'CS2301', 1, 1, 2023),
                                                                              (2, 1, '计科2302班', 'CS2302', 2, 1, 2023),
                                                                              (3, 2, '软工2301班', 'SE2301', 1, 1, 2023),
                                                                              (4, 3, '英语2301班', 'ENG2301', 1, 1, 2023),
                                                                              (5, 4, '机械2301班', 'ME2301', 1, 1, 2023),
                                                                              (6, 5, '数学2301班', 'MATH2301', 1, 1, 2023);

-- 扩充用户：admin、教师、学生；密码统一：123456
INSERT INTO sys_user(id, username, password, real_name, gender, email, phone, avatar, role, status, class_id, college_id, major_id, last_login_time) VALUES
-- 管理员
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 'admin@smart.edu', '13800000001', '/avatar/default.png', 'admin', 1, NULL, NULL, NULL, '2026-08-18 09:20:10'),

-- 教师
(2, 't001', 'e10adc3949ba59abbe56e057f20f883e', '张教授', 1, 'zhang@smart.edu', '13800000002', '/avatar/t001.png', 'teacher', 1, NULL, 1, 1, '2026-08-18 16:30:22'),
(3, 't002', 'e10adc3949ba59abbe56e057f20f883e', '李老师', 2, 'li@smart.edu', '13800000003', '/avatar/t002.png', 'teacher', 1, NULL, 1, 2, '2026-08-18 14:10:05'),
(4, 't003', 'e10adc3949ba59abbe56e057f20f883e', '王工', 1, 'wang@smart.edu', '13800000007', '/avatar/t003.png', 'teacher', 1, NULL, 4, 5, '2026-08-20 09:10:00'),
(5, 't004', 'e10adc3949ba59abbe56e057f20f883e', '陈数理', 1, 'chen@smart.edu', '13800000008', '/avatar/t004.png', 'teacher', 1, NULL, 5, 6, '2026-08-21 10:20:00'),

-- 计科2301班 (class_id=1) 6人
(6, 's230101', 'e10adc3949ba59abbe56e057f20f883e', '莫能涛', 1, 's230101@smart.edu', '13800000101', '/avatar/default.png', 'student', 1, 1, 1, 1, '2026-08-18 15:45:33'),
(7, 's230102', 'e10adc3949ba59abbe56e057f20f883e', '王浩', 1, 's230102@smart.edu', '13800000102', '/avatar/default.png', 'student', 1, 1, 1, 1, '2026-08-18 11:20:11'),
(8, 's230103', 'e10adc3949ba59abbe56e057f20f883e', '李雪婷', 2, 's230103@smart.edu', '13800000103', '/avatar/default.png', 'student', 1, 1, 1, 1, '2026-08-19 09:15:00'),
(9, 's230104', 'e10adc3949ba59abbe56e057f20f883e', '赵明辉', 1, 's230104@smart.edu', '13800000104', '/avatar/default.png', 'student', 1, 1, 1, 1, '2026-08-20 14:20:00'),
(10, 's230105', 'e10adc3949ba59abbe56e057f20f883e', '孙雨桐', 2, 's230105@smart.edu', '13800000105', '/avatar/default.png', 'student', 1, 1, 1, 1, '2026-08-21 10:30:00'),
(11, 's230106', 'e10adc3949ba59abbe56e057f20f883e', '周子涵', 1, 's230106@smart.edu', '13800000106', '/avatar/default.png', 'student', 1, 1, 1, 1, '2026-08-22 08:50:00'),

-- 计科2302班 (class_id=2) 6人
(12, 's230201', 'e10adc3949ba59abbe56e057f20f883e', '刘思琪', 2, 's230201@smart.edu', '13800000201', '/avatar/default.png', 'student', 1, 2, 1, 1, '2026-08-18 10:05:44'),
(13, 's230202', 'e10adc3949ba59abbe56e057f20f883e', '陈嘉豪', 1, 's230202@smart.edu', '13800000202', '/avatar/default.png', 'student', 1, 2, 1, 1, '2026-08-19 16:30:00'),
(14, 's230203', 'e10adc3949ba59abbe56e057f20f883e', '张梦瑶', 2, 's230203@smart.edu', '13800000203', '/avatar/default.png', 'student', 1, 2, 1, 1, '2026-08-21 08:45:00'),
(15, 's230204', 'e10adc3949ba59abbe56e057f20f883e', '郑宇航', 1, 's230204@smart.edu', '13800000204', '/avatar/default.png', 'student', 1, 2, 1, 1, '2026-08-22 09:20:00'),
(16, 's230205', 'e10adc3949ba59abbe56e057f20f883e', '林小雅', 2, 's230205@smart.edu', '13800000205', '/avatar/default.png', 'student', 1, 2, 1, 1, '2026-08-23 14:10:00'),
(17, 's230206', 'e10adc3949ba59abbe56e057f20f883e', '黄俊杰', 1, 's230206@smart.edu', '13800000206', '/avatar/default.png', 'student', 1, 2, 1, 1, '2026-08-24 11:00:00'),

-- 软工2301班 (class_id=3) 3人
(18, 's230301', 'e10adc3949ba59abbe56e057f20f883e', '赵宇', 1, 's230301@smart.edu', '13800000301', '/avatar/default.png', 'student', 1, 3, 1, 2, '2026-08-22 08:30:00'),
(19, 's230302', 'e10adc3949ba59abbe56e057f20f883e', '吴欣怡', 2, 's230302@smart.edu', '13800000302', '/avatar/default.png', 'student', 1, 3, 1, 2, '2026-08-25 09:40:00'),
(20, 's230303', 'e10adc3949ba59abbe56e057f20f883e', '何佳乐', 1, 's230303@smart.edu', '13800000303', '/avatar/default.png', 'student', 1, 3, 1, 2, '2026-08-26 08:20:00'),

-- 英语2301班 (class_id=4) 2人
(21, 's230401', 'e10adc3949ba59abbe56e057f20f883e', '周佳', 2, 's230401@smart.edu', '13800000401', '/avatar/default.png', 'student', 1, 4, 2, 3, '2026-08-22 08:35:00'),
(22, 's230402', 'e10adc3949ba59abbe56e057f20f883e', '徐天乐', 1, 's230402@smart.edu', '13800000402', '/avatar/default.png', 'student', 1, 4, 2, 3, '2026-08-23 14:30:00'),

-- 机械2301班 (class_id=5) 2人
(23, 's230501', 'e10adc3949ba59abbe56e057f20f883e', '吴帆', 1, 's230501@smart.edu', '13800000501', '/avatar/default.png', 'student', 1, 5, 4, 5, '2026-08-22 08:40:00'),
(24, 's230502', 'e10adc3949ba59abbe56e057f20f883e', '许志强', 1, 's230502@smart.edu', '13800000502', '/avatar/default.png', 'student', 1, 5, 4, 5, '2026-08-24 09:15:00'),

-- 数学2301班 (class_id=6) 1人
(25, 's230601', 'e10adc3949ba59abbe56e057f20f883e', '唐雅琴', 2, 's230601@smart.edu', '13800000601', '/avatar/default.png', 'student', 1, 6, 5, 6, '2026-08-23 10:00:00');

-- 扩充课程
INSERT INTO course(id, code, name, teacher_id, credit, semester, capacity, description, status) VALUES
                                                                                                    (1, 'CS23001', 'Java程序设计', 2, 4.00, '2025-2026-2', 60, 'Java面向对象、SpringBoot基础开发课程', 1),
                                                                                                    (2, 'CS23002', '数据结构与算法', 3, 3.50, '2025-2026-2', 55, '线性表、树、图、排序查找算法', 1),
                                                                                                    (3, 'CS23003', '计算机网络', 2, 3.00, '2025-2026-2', 50, 'TCP/IP、HTTP协议、网络分层原理', 1),
                                                                                                    (4, 'CS24001', 'SpringBoot框架开发', 2, 3.50, '2026-2027-1', 60, 'SpringBoot、MyBatis-Plus、前后端联调', 1),
                                                                                                    (5, 'CS24002', '操作系统', 3, 3.00, '2026-2027-1', 50, '进程线程、内存管理、IO调度', 1),
                                                                                                    (6, 'ME24001', '机械制图', 4, 4.00, '2026-2027-1', 40, 'CAD制图、机械零件图纸绘制', 1),
                                                                                                    (7, 'MA24001', '高等数学进阶', 5, 3.00, '2026-2027-1', 45, '多元微积分、级数、微分方程', 1);

-- 排课数据
INSERT INTO course_schedule(course_id, teacher_id, class_id, week, day, lesson, room, color, status) VALUES
                                                                                                         (1, 2, 1, '[3,4,5,6]', 1, 2, 'A102', '#409EFF', 1),
                                                                                                         (1, 2, 1, '[3,4,5,6]', 3, 4, 'A102', '#409EFF', 1),
                                                                                                         (2, 3, 1, '[3,4,5,6]', 2, 1, 'B303', '#67C23A', 1),
                                                                                                         (3, 2, 2, '[3,4,5,6,7]', 4, 3, 'C205', '#E6A23C', 1),
                                                                                                         (4, 2, 1, '[1,2,3,4,5]', 2, 2, 'A203', '#F56C6C', 1),
                                                                                                         (5, 3, 2, '[1,2,3,4,5]', 4, 1, 'B104', '#909399', 1),
                                                                                                         (6, 4, 5, '[1,2,3,4]', 1, 3, 'D401', '#00BCD4', 1),
                                                                                                         (7, 5, 6, '[1,2,3,4]', 3, 2, 'E202', '#673AB7', 1);

INSERT INTO selection_config (id, start_time, end_time, min_credit, max_credit, scope_type, scope_value, status)
VALUES (1, '2026-08-01 08:00:00', '2026-08-20 23:59:59', 2.00, 10.00, 'all', NULL, 1);

-- 选课记录
INSERT INTO selection_record(id, student_id, course_id, selected_time, status) VALUES
-- 计科2301班 选课
(1, 6, 1, '2026-08-02 10:12:33', 1),
(2, 6, 2, '2026-08-02 10:14:11', 1),
(3, 7, 1, '2026-08-02 11:05:22', 1),
(4, 7, 3, '2026-08-02 11:08:45', 1),
(5, 8, 1, '2026-08-03 09:20:00', 1),
(6, 8, 2, '2026-08-03 09:25:00', 1),
(7, 9, 1, '2026-08-03 10:00:00', 1),
(8, 9, 4, '2026-08-03 10:05:00', 1),
(9, 10, 2, '2026-08-04 14:00:00', 1),
(10, 10, 4, '2026-08-04 14:05:00', 1),
(11, 11, 1, '2026-08-05 09:00:00', 1),
(12, 11, 3, '2026-08-05 09:05:00', 1),

-- 计科2302班 选课
(13, 12, 1, '2026-08-02 10:12:33', 1),
(14, 12, 2, '2026-08-02 10:14:11', 1),
(15, 13, 1, '2026-08-02 11:05:22', 1),
(16, 13, 3, '2026-08-02 11:08:45', 1),
(17, 14, 2, '2026-08-03 09:33:10', 1),
(18, 14, 4, '2026-08-03 09:35:00', 1),
(19, 15, 1, '2026-08-04 13:00:00', 1),
(20, 15, 3, '2026-08-04 13:05:00', 1),
(21, 16, 2, '2026-08-05 10:00:00', 1),
(22, 16, 4, '2026-08-05 10:05:00', 1),
(23, 17, 1, '2026-08-06 11:00:00', 1),
(24, 17, 2, '2026-08-06 11:05:00', 1),

-- 软工2301班 选课
(25, 18, 5, '2026-08-06 10:00:00', 1),
(26, 18, 6, '2026-08-06 10:05:00', 1),
(27, 19, 5, '2026-08-07 09:00:00', 1),
(28, 19, 7, '2026-08-07 09:05:00', 1),
(29, 20, 5, '2026-08-08 08:30:00', 1),
(30, 20, 6, '2026-08-08 08:35:00', 1);

-- 成绩数据
INSERT INTO student_score(id, course_id, student_id, teacher_id, score, usual_score, final_score, remark, status) VALUES

-- 课程1: Java程序设计 (teacher_id=2)
(1, 1, 6, 2, 82.50, 85.00, 80.00, '平时出勤良好', 1),
(2, 1, 7, 2, 88.00, 90.00, 86.00, '表现优秀', 1),
(3, 1, 8, 2, 75.00, 72.00, 78.00, '作业完成度一般', 1),
(4, 1, 9, 2, 91.00, 92.00, 90.00, '成绩优异', 1),
(5, 1, 10, 2, 68.00, 65.00, 71.00, '需要加强基础', 1),
(6, 1, 11, 2, 55.00, 50.00, 60.00, '期末发挥失常，不及格', 1),
(7, 1, 12, 2, 86.50, 88.00, 85.00, '表现良好', 1),
(8, 1, 13, 2, 92.00, 95.00, 90.00, '成绩优秀', 1),
(9, 1, 15, 2, 78.00, 76.00, 80.00, '', 1),
(10, 1, 17, 2, 45.00, 40.00, 50.00, '基础薄弱，严重不及格', 1),

-- 课程2: 数据结构与算法 (teacher_id=3)
(11, 2, 6, 3, 76.00, 74.00, 78.00, '', 1),
(12, 2, 8, 3, 80.00, 78.00, 82.00, '掌握较好', 1),
(13, 2, 10, 3, 70.00, 68.00, 72.00, '', 1),
(14, 2, 12, 3, 65.00, 60.00, 70.00, '需要加强练习', 1),
(15, 2, 14, 3, 58.00, 55.00, 61.00, '勉强及格', 1),
(16, 2, 16, 3, 85.00, 88.00, 82.00, '表现优秀', 1),
(17, 2, 17, 3, 48.00, 45.00, 51.00, '基础薄弱，不及格', 1),

-- 课程3: 计算机网络 (teacher_id=2)
(18, 3, 7, 2, 79.50, 77.00, 82.00, '', 1),
(19, 3, 11, 2, 84.00, 86.00, 82.00, '掌握较好', 1),
(20, 3, 13, 2, 62.00, 60.00, 64.00, '', 1),
(21, 3, 15, 2, 73.00, 70.00, 76.00, '', 1),

-- 课程4: SpringBoot框架开发 (teacher_id=2)
(22, 4, 9, 2, 88.00, 90.00, 86.00, '框架掌握熟练', 1),
(23, 4, 10, 2, 72.00, 70.00, 74.00, '', 1),
(24, 4, 14, 2, 55.00, 45.00, 60.00, '期末发挥失常，不及格', 1),
(25, 4, 16, 2, 80.00, 78.00, 82.00, '表现良好', 1),

-- 课程5: 操作系统 (teacher_id=3)
(26, 5, 18, 3, 48.00, 40.00, 56.00, '基础薄弱，不及格', 1),
(27, 5, 19, 3, 65.00, 62.00, 68.00, '', 1),
(28, 5, 20, 3, 72.00, 70.00, 74.00, '', 1),

-- 课程6: 机械制图 (teacher_id=4)
(29, 6, 18, 4, 72.00, 70.00, 74.00, '', 1),
(30, 6, 20, 4, 68.00, 65.00, 71.00, '需要加强练习', 1),

-- 课程7: 高等数学进阶 (teacher_id=5)
(31, 7, 19, 5, 91.00, 92.00, 90.00, '成绩优异', 1);

-- 考勤会话
INSERT INTO attendance_session(id, course_id, teacher_id, class_id, session_date, start_time, end_time, check_code, status) VALUES
                                                                                                                                (1, 1, 2, 1, '2026-08-12', '08:00:00', '09:40:00', '8A72K9', 0),
                                                                                                                                (2, 2, 3, 1, '2026-08-13', '10:00:00', '11:40:00', 'B3D5Q7', 0),
                                                                                                                                (3, 4, 2, 1, '2026-08-22', '08:00:00', '09:40:00', 'C9X3R5', 1),
                                                                                                                                (4, 5, 3, 2, '2026-08-23', '10:00:00', '11:40:00', 'D2Y7S8', 1);

-- 考勤记录
INSERT INTO attendance_record(id, session_id, student_id, longitude, latitude, checkin_time, status) VALUES
                                                                                                         (1, 1, 6, 113.392340, 23.129100, '2026-08-12 08:04:22', 1),
                                                                                                         (2, 1, 7, 113.392360, 23.129120, '2026-08-12 08:06:10', 1),
                                                                                                         (3, 1, 8, 113.392350, 23.129105, '2026-08-12 08:05:30', 1),
                                                                                                         (4, 1, 9, 113.392345, 23.129110, '2026-08-12 08:07:00', 1),
                                                                                                         (5, 1, 10, 113.392355, 23.129115, '2026-08-12 08:08:15', 1),
                                                                                                         (6, 2, 6, 113.392345, 23.129110, '2026-08-13 10:02:33', 1),
                                                                                                         (7, 2, 8, 113.392350, 23.129105, '2026-08-13 10:03:00', 1),
                                                                                                         (8, 2, 10, 113.392355, 23.129115, '2026-08-13 10:03:41', 1),
                                                                                                         (9, 3, 6, 113.392342, 23.129105, '2026-08-22 08:03:10', 1),
                                                                                                         (10, 3, 7, 113.392344, 23.129108, '2026-08-22 08:05:20', 1),
                                                                                                         (11, 4, 12, 113.392350, 23.129130, '2026-08-23 10:01:15', 1);

-- 公告
INSERT INTO notice(id, title, content, publisher_id, is_top, status) VALUES
                                                                         (1, '2026秋季选课通知', '各位同学：2026秋季学期选课已经开放，请在规定时间内完成选课操作。', 1, 1, 1),
                                                                         (2, '期末考试安排', '本学期期末考试时间已发布，请留意课程通知，做好复习。', 1, 0, 1),
                                                                         (3, '成绩录入通知', '各位教师，请于8月30日前完成课程期末成绩录入。', 1, 0, 1),
                                                                         (4, '学业预警提醒', '部分同学多门课程成绩偏低，请及时联系任课教师答疑。', 1, 0, 1);

-- 公告阅读记录
INSERT INTO notice_read_record(id, notice_id, user_id, read_time) VALUES
                                                                      (1, 1, 6, '2026-08-02 14:22:10'),
                                                                      (2, 1, 7, '2026-08-02 15:10:33'),
                                                                      (3, 1, 8, '2026-08-03 08:44:21'),
                                                                      (4, 2, 6, '2026-08-05 09:11:02'),
                                                                      (5, 3, 6, '2026-08-24 10:10:00'),
                                                                      (6, 3, 7, '2026-08-24 10:12:00'),
                                                                      (7, 4, 12, '2026-08-25 09:00:00');

-- 问答
INSERT INTO qa_question(id, user_id, title, content, tags, is_top, like_count, reply_count, status) VALUES
                                                                                                        (1, 6, 'SpringBoot启动报错怎么排查', '我的SpringBoot项目启动报404，接口访问不到，该怎么定位？', 'SpringBoot,后端,bug', 0, 12, 2, 1),
                                                                                                        (2, 7, '数据结构快速排序实现思路', '求讲解快排的原理与Java代码示例', '算法,Java', 0, 7, 1, 1),
                                                                                                        (3, 12, '操作系统进程调度算法讲解', '想了解时间片轮转、FCFS调度算法区别', '操作系统,理论', 0, 5, 0, 1),
                                                                                                        (4, 18, '机械制图CAD绘图技巧', 'CAD画零件图有哪些常用快捷键与绘图规范', '机械,CAD', 0, 3, 0, 1);

INSERT INTO qa_reply(id, question_id, user_id, content, like_count, status) VALUES
                                                                                (1, 1, 2, '优先检查接口路径、@RestController、组件扫描包路径，查看控制台有无报错。', 8, 1),
                                                                                (2, 1, 3, '确认yml配置上下文路径是否修改，测试用postman直接访问接口。', 4, 1),
                                                                                (3, 2, 2, '快排核心：选基准值，分区，递归处理左右子数组。', 5, 1);

-- AI会话
INSERT INTO ai_session(id, user_id, title, model_name) VALUES
                                                           (1, 6, 'Java学习问答会话', 'gpt-4o-mini'),
                                                           (2, 7, '算法问题咨询', 'gpt-4o-mini'),
                                                           (3, 12, '操作系统学习', 'gpt-4o-mini'),
                                                           (4, 18, '机械制图辅助', 'gpt-4o-mini');

INSERT INTO ai_message(id, session_id, sender, content) VALUES
                                                            (1, 1, 0, '帮我写一个SpringBoot全局异常处理示例'),
                                                            (2, 1, 1, '下面给你一份@RestControllerAdvice全局异常处理器完整代码……'),
                                                            (3, 2, 0, '讲解二叉树的层序遍历'),
                                                            (4, 2, 1, '二叉树层序遍历借助队列实现，一层一层输出节点……'),
                                                            (5, 3, 0, '讲解进程时间片轮转调度'),
                                                            (6, 3, 1, '时间片轮转将CPU时间切分成时间片，轮流分配给就绪队列进程……');


-- 课程评价数据
-- =====================================================
INSERT INTO course_evaluation (
    id, course_id, teacher_id, student_id, score,
    teaching_ability, class_atmosphere, knowledge_clarity, homework_feedback, qa_service,
    content
) VALUES
-- 课程1: Java程序设计 (teacher_id=2) 的评价
(1, 1, 2, 6, 4.80, 5.0, 4.5, 5.0, 4.5, 5.0, '课程讲解清晰，案例丰富，收获很大。'),
(2, 1, 2, 7, 4.60, 4.5, 4.5, 4.5, 4.5, 5.0, '老师讲课节奏很好，作业布置合理。'),
(3, 1, 2, 8, 4.20, 4.0, 4.0, 4.5, 4.0, 4.5, '内容充实，但语速稍快。'),
(4, 1, 2, 9, 4.90, 5.0, 5.0, 5.0, 4.5, 5.0, '非常优秀的老师，讲解深入浅出！'),
(5, 1, 2, 10, 3.80, 4.0, 3.5, 4.0, 3.5, 4.0, '课程难度较大，希望多增加练习时间。'),
(6, 1, 2, 11, 4.00, 4.0, 4.0, 4.0, 4.0, 4.0, '整体不错，但期末复习指导可以更充分。'),
(7, 1, 2, 12, 4.50, 4.5, 4.5, 4.5, 4.5, 4.5, '认真负责，教学水平高。'),
(8, 1, 2, 13, 4.70, 5.0, 4.5, 4.5, 4.5, 5.0, '非常耐心，课后答疑很及时。'),
(9, 1, 2, 15, 4.00, 4.0, 4.0, 4.0, 4.0, 4.0, '课程内容实用，建议增加更多实战案例。'),

-- 课程2: 数据结构与算法 (teacher_id=3) 的评价
(10, 2, 3, 6, 4.40, 4.5, 4.0, 4.5, 4.5, 4.5, '算法课难度偏高，希望多一点实操演示。'),
(11, 2, 3, 8, 4.20, 4.0, 4.0, 4.5, 4.5, 4.0, '理论讲解扎实，但代码演示略少。'),
(12, 2, 3, 10, 3.60, 3.5, 3.5, 4.0, 3.5, 3.5, '内容较难，需要更多基础铺垫。'),
(13, 2, 3, 12, 4.00, 4.0, 4.0, 4.0, 4.0, 4.0, '讲解清晰，但进度稍快。'),
(14, 2, 3, 14, 3.80, 4.0, 3.5, 4.0, 3.5, 4.0, '希望增加习题课和答疑时间。'),
(15, 2, 3, 16, 4.60, 4.5, 4.5, 4.5, 4.5, 5.0, '老师讲解很透彻，收获很大！'),
(16, 2, 3, 17, 3.50, 3.5, 3.5, 3.5, 3.5, 3.5, '课程难度太大，建议分层次教学。'),

-- 课程3: 计算机网络 (teacher_id=2) 的评价
(17, 3, 2, 7, 4.30, 4.5, 4.0, 4.5, 4.0, 4.5, '网络协议讲解清晰，实验课很有帮助。'),
(18, 3, 2, 11, 4.60, 4.5, 4.5, 4.5, 4.5, 5.0, '老师专业知识渊博，课堂生动。'),
(19, 3, 2, 13, 3.80, 4.0, 3.5, 4.0, 4.0, 3.5, '内容偏向理论，希望增加更多实际案例。'),
(20, 3, 2, 15, 4.10, 4.0, 4.0, 4.0, 4.5, 4.0, '整体不错，网络实验设备可以更新。'),

-- 课程4: SpringBoot框架开发 (teacher_id=2) 的评价
(21, 4, 2, 9, 4.80, 5.0, 4.5, 5.0, 5.0, 4.5, '框架掌握熟练，项目实战很有帮助！'),
(22, 4, 2, 10, 4.20, 4.0, 4.0, 4.5, 4.5, 4.0, '框架内容难度大，希望增加更多课堂练习。'),
(23, 4, 2, 14, 3.80, 4.0, 3.5, 4.0, 4.0, 3.5, '内容较新，希望补充更多文档和资料。'),
(24, 4, 2, 16, 4.50, 4.5, 4.5, 4.5, 4.5, 4.5, '老师实战经验丰富，课程很实用！'),

-- 课程5: 操作系统 (teacher_id=3) 的评价
(25, 5, 3, 18, 3.60, 3.5, 3.5, 4.0, 3.5, 3.5, '理论较多，希望补充代码演示和案例。'),
(26, 5, 3, 19, 4.00, 4.0, 4.0, 4.0, 4.0, 4.0, '讲解细致，但课堂互动可以更多。'),
(27, 5, 3, 20, 4.20, 4.5, 4.0, 4.5, 4.0, 4.0, '内容体系完整，实验指导很详细。'),

-- 课程6: 机械制图 (teacher_id=4) 的评价
(28, 6, 4, 18, 4.20, 4.5, 4.0, 4.0, 4.5, 4.0, 'CAD制图讲解实用，但软件操作演示可更多。'),
(29, 6, 4, 20, 4.00, 4.0, 4.0, 4.0, 4.0, 4.0, '制图规范讲解清楚，希望增加更多绘图练习。'),

-- 课程7: 高等数学进阶 (teacher_id=5) 的评价
(30, 7, 5, 19, 4.70, 5.0, 4.5, 5.0, 4.0, 4.5, '数学功底深厚，讲解逻辑清晰，非常受益！');
-- 学业预警
INSERT INTO warning_record(id, user_id, warning_type, level, title, content, status) VALUES
                                                                                         (1, 11, 'score', 2, '成绩预警', 'Java程序设计课程成绩55分，建议加强课后练习，及时向老师请教。', 1),
                                                                                         (2, 17, 'score', 3, '高风险成绩预警', 'Java程序设计45分，数据结构与算法48分，多门课程存在挂科风险。', 1),
                                                                                         (3, 14, 'score', 3, '高风险成绩预警', 'SpringBoot框架开发课程成绩55分，需要重点补习。', 1),
                                                                                         (4, 18, 'score', 3, '高风险成绩预警', '操作系统48分，不及格。', 1);

-- 仪表盘统计
INSERT INTO dashboard_stat(id, stat_type, target_date, target_id, value) VALUES
                                                                             (1, 'user_count', '2026-08-18', NULL, '{"admin":1,"teacher":4,"student":20}'),
                                                                             (2, 'course_count', '2026-08-18', NULL, '{"total":7,"open":7}');

-- =====================================================
-- 作业表数据 (homework)
-- =====================================================
INSERT INTO homework (id, course_id, teacher_id, title, content, attachment_url, attachment_name, deadline, status) VALUES
                                                                                                                        (1, 1, 2, 'Java程序设计-第一次作业', '完成一个简单的学生管理系统，包含增删改查功能，使用SpringBoot+MyBatis实现。', '/homework/attachments/hw1_springboot_student.docx', 'SpringBoot学生管理系统作业要求.docx', '2026-08-20 23:59:59', 1),
                                                                                                                        (2, 1, 2, 'Java程序设计-第二次作业', '实现一个简易的图书管理系统，包含RESTful API设计，使用JPA操作数据库。', '/homework/attachments/hw2_springboot_book.pdf', '图书管理系统API设计作业.pdf', '2026-09-05 23:59:59', 1),
                                                                                                                        (3, 2, 3, '数据结构-线性表作业', '实现ArrayList和LinkedList，并比较两者在增删改查操作上的性能差异。', '/homework/attachments/hw3_list.pdf', '线性表实现作业.pdf', '2026-08-22 23:59:59', 1),
                                                                                                                        (4, 2, 3, '数据结构-树与图作业', '实现二叉树的三种遍历方式（前序、中序、后序）以及图的邻接表表示和广度优先遍历。', '/homework/attachments/hw4_tree_graph.pdf', '树与图作业.pdf', '2026-09-08 23:59:59', 1),
                                                                                                                        (5, 3, 2, '计算机网络-协议分析作业', '使用Wireshark抓包分析TCP三次握手和HTTP请求响应过程，提交分析报告。', '/homework/attachments/hw5_network.docx', '网络协议分析作业.docx', '2026-08-25 23:59:59', 1),
                                                                                                                        (6, 4, 2, 'SpringBoot-整合MyBatis作业', '完成SpringBoot整合MyBatis-Plus的CRUD操作，包含分页查询和条件构造器使用。', '/homework/attachments/hw6_mybatis.pdf', 'SpringBoot整合MyBatis作业.pdf', '2026-09-10 23:59:59', 1),
                                                                                                                        (7, 5, 3, '操作系统-进程调度作业', '模拟实现FCFS、SJF、时间片轮转三种进程调度算法，输出调度序列和等待时间。', '/homework/attachments/hw7_scheduler.pdf', '进程调度算法作业.pdf', '2026-09-12 23:59:59', 1),
                                                                                                                        (8, 6, 4, '机械制图-零件图绘制作业', '根据提供的三视图，用CAD绘制完整的零件图，标注尺寸和技术要求。', '/homework/attachments/hw8_cad.dwg', '零件图绘制作业.dwg', '2026-09-15 23:59:59', 1),
                                                                                                                        (9, 7, 5, '高等数学-多元微积分作业', '完成第八章多元函数微分学课后习题1-20题，需提交详细解题步骤。', '/homework/attachments/hw9_math.pdf', '多元微积分作业.pdf', '2026-09-18 23:59:59', 1),
                                                                                                                        (10, 1, 2, 'Java程序设计-第三次作业', '实现一个简单的在线购物车系统，包含用户登录、商品浏览、加入购物车、下单等功能。', '/homework/attachments/hw10_shopping.pdf', '购物车系统作业.pdf', '2026-09-20 23:59:59', 1);

-- =====================================================
-- 作业提交表数据 (homework_submission)
-- =====================================================
INSERT INTO homework_submission (id, homework_id, student_id, content, attachment_url, attachment_name, score, comment, submit_time, grade_time, status) VALUES
-- 作业1: Java程序设计-第一次作业 (homework_id=1)
(1, 1, 6, '已完成学生管理系统的增删改查功能，使用SpringBoot+MyBatis+MySQL实现，包含单元测试。', '/submissions/hw1/s230101_student_system.zip', '学生管理系统源码.zip', 85.00, '功能完整，代码规范，建议增加异常处理。', '2026-08-18 14:20:00', '2026-08-21 09:30:00', 1),
(2, 1, 7, '实现了学生管理，包含分页查询和条件搜索。', '/submissions/hw1/s230102_student_system.zip', '学生管理系统源码.zip', 78.00, '功能基本实现，但分页查询有bug需要修复。', '2026-08-19 10:15:00', '2026-08-21 09:35:00', 1),
(3, 1, 8, '完成了所有CRUD操作，并补充了Swagger接口文档。', '/submissions/hw1/s230103_student_system.zip', '学生管理系统源码.zip', 90.00, '实现优秀，代码质量高，接口文档完善。', '2026-08-18 20:30:00', '2026-08-21 09:40:00', 1),
(4, 1, 9, '实现增删改查功能，使用JPA操作数据库。', '/submissions/hw1/s230104_student_system.zip', '学生管理系统源码.zip', 82.00, '功能实现较好，但部分接口设计可优化。', '2026-08-19 16:45:00', '2026-08-21 09:45:00', 1),
(5, 1, 10, '实现了基本的学生管理功能。', '/submissions/hw1/s230105_student_system.zip', '学生管理系统源码.zip', 65.00, '功能较简单，未完全覆盖所有要求，需加强练习。', '2026-08-20 08:00:00', '2026-08-21 09:50:00', 1),
(6, 1, 11, '没有完全完成，只实现了查询和添加功能。', '/submissions/hw1/s230106_student_system.zip', '学生管理系统源码.zip', 45.00, '完成度不足，请课后补习并重新提交。', '2026-08-20 11:20:00', '2026-08-21 09:55:00', 1),
(7, 1, 12, '实现了完整的增删改查，加入全局异常处理和日志记录。', '/submissions/hw1/s230201_student_system.zip', '学生管理系统源码.zip', 88.00, '实现较好，异常处理和日志记录为加分项。', '2026-08-18 15:00:00', '2026-08-21 10:00:00', 1),
(8, 1, 13, '实现了基本的CRUD功能。', '/submissions/hw1/s230202_student_system.zip', '学生管理系统源码.zip', 75.00, '功能完整但代码结构可优化，注意分层设计。', '2026-08-19 13:20:00', '2026-08-21 10:05:00', 1),
(9, 1, 15, '完成增删改查，并加入Redis缓存优化查询性能。', '/submissions/hw1/s230204_student_system.zip', '学生管理系统源码.zip', 80.00, 'Redis缓存使用正确，但需要考虑缓存一致性。', '2026-08-20 09:40:00', '2026-08-21 10:10:00', 1),
(10, 1, 17, '只完成了查和删功能，增改功能未实现。', '/submissions/hw1/s230206_student_system.zip', '学生管理系统源码.zip', 30.00, '未按要求完成，请尽快补交。', '2026-08-20 22:00:00', '2026-08-21 10:15:00', 1),

-- 作业2: Java程序设计-第二次作业 (homework_id=2)
(11, 2, 6, '完成了图书管理系统的RESTful API，包含分页、过滤和排序功能。', '/submissions/hw2/s230101_book_api.zip', '图书管理系统API.zip', 87.00, 'API设计规范，文档齐全，建议补充集成测试。', '2026-09-03 14:00:00', '2026-09-06 08:30:00', 1),
(12, 2, 7, '实现了图书的增删改查API。', '/submissions/hw2/s230102_book_api.zip', '图书管理系统API.zip', 72.00, '基础功能完整，但缺少参数校验和统一返回格式。', '2026-09-04 09:30:00', '2026-09-06 08:35:00', 1),
(13, 2, 9, '设计规范的RESTful API，使用Spring Data JPA，包含分页和排序。', '/submissions/hw2/s230104_book_api.zip', '图书管理系统API.zip', 92.00, 'API设计优秀，代码简洁清晰。', '2026-09-03 20:00:00', '2026-09-06 08:40:00', 1),

-- 作业3: 数据结构-线性表作业 (homework_id=3)
(14, 3, 6, '实现了ArrayList和LinkedList，性能对比测试报告已附在文档中。', '/submissions/hw3/s230101_list.zip', '线性表实现源码.zip', 80.00, '实现正确，性能测试报告详细。', '2026-08-20 16:30:00', '2026-08-23 10:00:00', 1),
(15, 3, 8, '实现了两种List，并完成了性能对比。', '/submissions/hw3/s230103_list.zip', '线性表实现源码.zip', 76.00, '实现基本正确，部分方法效率可优化。', '2026-08-21 08:20:00', '2026-08-23 10:05:00', 1),
(16, 3, 10, '实现了ArrayList和LinkedList，但性能测试数据不完整。', '/submissions/hw3/s230105_list.zip', '线性表实现源码.zip', 60.00, '代码有部分bug，需要修复；性能测试需补充。', '2026-08-22 11:00:00', '2026-08-23 10:10:00', 1),
(17, 3, 12, '完成了两种List的完整实现和详细性能对比分析。', '/submissions/hw3/s230201_list.zip', '线性表实现源码.zip', 85.00, '实现优秀，分析报告非常到位。', '2026-08-21 13:40:00', '2026-08-23 10:15:00', 1),
(18, 3, 14, '实现了ArrayList和LinkedList。', '/submissions/hw3/s230203_list.zip', '线性表实现源码.zip', 68.00, '功能实现基本完整，但部分方法效率较低。', '2026-08-22 09:00:00', '2026-08-23 10:20:00', 1),

-- 作业4: 数据结构-树与图作业 (homework_id=4)
(19, 4, 6, '实现二叉树三种遍历和图邻接表BFS。', '/submissions/hw4/s230101_tree_graph.zip', '树与图实现源码.zip', 78.00, '实现正确，建议补充迭代方式的遍历实现。', '2026-09-06 10:00:00', '2026-09-09 14:00:00', 1),
(20, 4, 8, '完成二叉树的递归遍历和图邻接表表示，BFS实现正确。', '/submissions/hw4/s230103_tree_graph.zip', '树与图实现源码.zip', 82.00, '实现较好，代码规范。', '2026-09-07 08:30:00', '2026-09-09 14:05:00', 1),
(21, 4, 12, '完整实现了所有要求的算法。', '/submissions/hw4/s230201_tree_graph.zip', '树与图实现源码.zip', 90.00, '代码高效，注释清晰，非常优秀。', '2026-09-06 19:00:00', '2026-09-09 14:10:00', 1),
(22, 4, 16, '实现了树的遍历和图BFS，但图邻接表实现略有瑕疵。', '/submissions/hw4/s230205_tree_graph.zip', '树与图实现源码.zip', 70.00, '有部分bug，需要修复图的边添加逻辑。', '2026-09-07 20:30:00', '2026-09-09 14:15:00', 1),

-- 作业5: 计算机网络-协议分析作业 (homework_id=5)
(23, 5, 7, '已完成Wireshark抓包分析报告，包含TCP三次握手和HTTP请求响应详细分析。', '/submissions/hw5/s230102_network_analysis.docx', '网络分析报告.docx', 86.00, '分析详细，抓包截图清晰，报告格式规范。', '2026-08-23 09:00:00', '2026-08-26 15:00:00', 1),
(24, 5, 11, '提交了TCP和HTTP的分析报告。', '/submissions/hw5/s230106_network_analysis.docx', '网络分析报告.docx', 74.00, '内容基本完整，但分析深度不够。', '2026-08-24 14:30:00', '2026-08-26 15:05:00', 1),
(25, 5, 13, '详细的抓包分析，包含时序图和状态分析。', '/submissions/hw5/s230202_network_analysis.docx', '网络分析报告.docx', 90.00, '分析非常详尽，加入时序图是加分项。', '2026-08-23 22:00:00', '2026-08-26 15:10:00', 1),
(26, 5, 15, '完成抓包分析和报告撰写。', '/submissions/hw5/s230204_network_analysis.docx', '网络分析报告.docx', 70.00, '内容完整但格式不够规范，部分术语使用有误。', '2026-08-24 16:20:00', '2026-08-26 15:15:00', 1),

-- 作业6: SpringBoot-整合MyBatis作业 (homework_id=6)
(27, 6, 9, '完成SpringBoot+MyBatis-Plus整合，包含CRUD、分页和条件构造器。', '/submissions/hw6/s230104_mybatis.zip', 'MyBatis整合源码.zip', 88.00, '整合正确，代码规范，条件构造器使用熟练。', '2026-09-08 15:00:00', '2026-09-11 09:00:00', 1),
(28, 6, 10, '实现了基本的CRUD和分页查询。', '/submissions/hw6/s230105_mybatis.zip', 'MyBatis整合源码.zip', 72.00, '功能基本实现，但条件构造器的使用可以更丰富。', '2026-09-09 10:00:00', '2026-09-11 09:05:00', 1),
(29, 6, 14, '完成MyBatis-Plus整合，并加入逻辑删除功能。', '/submissions/hw6/s230203_mybatis.zip', 'MyBatis整合源码.zip', 80.00, '逻辑删除实现正确，整体完成度较高。', '2026-09-08 20:00:00', '2026-09-11 09:10:00', 1),
(30, 6, 16, '实现了CRUD和分页查询。', '/submissions/hw6/s230205_mybatis.zip', 'MyBatis整合源码.zip', 75.00, '功能完整，但建议学习使用更多MyBatis-Plus特性。', '2026-09-09 14:00:00', '2026-09-11 09:15:00', 1),

-- 作业7: 操作系统-进程调度作业 (homework_id=7)
(31, 7, 18, '实现了FCFS、SJF、时间片轮转三种调度算法，附测试结果和对比分析。', '/submissions/hw7/s230301_scheduler.zip', '进程调度源码.zip', 78.00, '三种算法实现正确，分析报告完整。', '2026-09-10 11:00:00', '2026-09-13 16:00:00', 1),
(32, 7, 19, '完成三种调度算法的模拟实现。', '/submissions/hw7/s230302_scheduler.zip', '进程调度源码.zip', 70.00, '功能实现基本正确，SJF调度实现有轻微偏差。', '2026-09-11 08:30:00', '2026-09-13 16:05:00', 1),
(33, 7, 20, '实现了三种调度算法，并加入图形化输出调度序列。', '/submissions/hw7/s230303_scheduler.zip', '进程调度源码.zip', 85.00, '图形化输出是创新点，调度逻辑完全正确。', '2026-09-10 22:00:00', '2026-09-13 16:10:00', 1),

-- 作业8: 机械制图-零件图绘制作业 (homework_id=8)
(34, 8, 18, '完成零件图绘制，包含所有尺寸标注和技术要求。', '/submissions/hw8/s230301_part.dwg', '零件图.dwg', 82.00, '绘制规范，尺寸标注完整，技术要求合理。', '2026-09-13 10:00:00', '2026-09-16 14:30:00', 1),
(35, 8, 20, '绘制了零件图，标注了主要尺寸。', '/submissions/hw8/s230303_part.dwg', '零件图.dwg', 70.00, '图形基本正确，部分尺寸标注不完整。', '2026-09-14 09:00:00', '2026-09-16 14:35:00', 1),

-- 作业9: 高等数学-多元微积分作业 (homework_id=9)
(36, 9, 19, '完成第八章课后习题1-20题，包含详细解题步骤。', '/submissions/hw9/s230302_math.pdf', '多元微积分作业.pdf', 88.00, '解题步骤详细，答案正确率高，字迹工整。', '2026-09-16 16:00:00', '2026-09-19 09:00:00', 1),

-- 作业10: Java程序设计-第三次作业 (homework_id=10)
(37, 10, 6, '完成在线购物车系统，包含登录、购物车和下单功能。', '/submissions/hw10/s230101_shopping.zip', '购物车系统源码.zip', NULL, NULL, '2026-09-18 20:00:00', NULL, 1),
(38, 10, 7, '实现了商品浏览和购物车功能。', '/submissions/hw10/s230102_shopping.zip', '购物车系统源码.zip', NULL, NULL, '2026-09-19 09:30:00', NULL, 1),
(39, 10, 9, '完成全部功能，包含用户登录、商品管理、购物车和订单生成。', '/submissions/hw10/s230104_shopping.zip', '购物车系统源码.zip', NULL, NULL, '2026-09-18 18:00:00', NULL, 1),
(40, 10, 11, '基本完成了登录和浏览功能，购物车部分未完成。', '/submissions/hw10/s230106_shopping.zip', '购物车系统源码.zip', NULL, NULL, '2026-09-19 20:00:00', NULL, 1);


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
-- 说明：MySQL 主要存放“真实业务数据、主数据、事务数据、永久记录”，
-- Redis 负责“高频缓存、临时状态、会话/验证码、限流与热点数据”。
-- 结束

-- # 删库语句
-- DROP DATABASE IF EXISTS smart_teaching;

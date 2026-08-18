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


USE smart_teaching;

-- ==============================
-- 0.初始化角色数据（必须先插入）
-- ==============================
INSERT INTO sys_role(id, code, name, description) VALUES
                                                      (1, 'admin', '管理员', '平台管理员'),
                                                      (2, 'teacher', '教师', '任课教师'),
                                                      (3, 'student', '学生', '学生用户');

-- ==============================
-- 1.学院 org_college
-- ==============================
INSERT INTO org_college(name,code,parent_id,sort,status) VALUES
                                                             ('计算机科学与工程学院','CS01',0,1,1),
                                                             ('外国语学院','FL01',0,2,1),
                                                             ('经济管理学院','EM01',0,3,1);

-- ==============================
-- 2.专业 org_major
-- ==============================
INSERT INTO org_major(college_id,name,code,parent_id,sort,status) VALUES
                                                                      (1,'计算机科学与技术','CS-2023',0,1,1),
                                                                      (1,'软件工程','SE-2023',0,2,1),
                                                                      (2,'英语','ENG-2023',0,1,1),
                                                                      (3,'工商管理','MBA-2023',0,1,1);

-- ==============================
-- 3.班级 org_class
-- ==============================
INSERT INTO org_class(major_id,name,code,parent_id,sort,status,grade_year) VALUES
                                                                               (1,'计科2301班','CS2301',0,1,1,2023),
                                                                               (1,'计科2302班','CS2302',0,2,1,2023),
                                                                               (2,'英语2301班','ENG2301',0,1,1,2023),
                                                                               (3,'工管2301班','MBA2301',0,1,1,2023);

-- ==============================
-- 4.sys_user 用户 密码全部123456
-- ==============================
INSERT INTO sys_user(username,password,real_name,gender,email,phone,avatar,role,status,class_id,college_id,major_id,last_login_time) VALUES
                                                                                                                                         ('admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','系统管理员',1,'admin@smart.edu','13800000001','/avatar/admin.png','admin',1,NULL,NULL,NULL,'2026-08-15 09:20:10'),
                                                                                                                                         ('t001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','张教授',1,'zhang@smart.edu','13800000002','/avatar/t001.png','teacher',1,NULL,1,1,'2026-08-14 16:30:22'),
                                                                                                                                         ('t002','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','李老师',2,'li@smart.edu','13800000003','/avatar/t002.png','teacher',1,NULL,1,2,'2026-08-14 15:10:05'),
                                                                                                                                         ('s230101','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','莫能涛',1,'s230101@smart.edu','13800001001','/avatar/s01.png','student',1,1,1,1,'2026-08-15 08:45:33'),
                                                                                                                                         ('s230102','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','王浩',1,'s230102@smart.edu','13800001002','/avatar/s02.png','student',1,1,1,1,'2026-08-15 08:33:12'),
                                                                                                                                         ('s230201','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','刘思琪',2,'s230201@smart.edu','13800002001','/avatar/s03.png','student',1,2,1,1,'2026-08-14 22:10:44');

-- ==============================
-- 5.sys_user_role 用户角色关联
-- ==============================
INSERT INTO sys_user_role(user_id,role_id) VALUES
                                               (1,1),
                                               (2,2),
                                               (3,2),
                                               (4,3),
                                               (5,3),
                                               (6,3);

-- ==============================
-- 6.course 课程表（已修正）
-- ==============================
INSERT INTO course(code,name,teacher_id,credit,semester,capacity,description,status) VALUES
                                                                                         ('CS23001','Java程序设计',2,4.00,'2025-2026-2',60,'Java面向对象、SpringBoot基础开发课程',1),
                                                                                         ('CS23002','数据结构与算法',3,3.50,'2025-2026-2',55,'线性表、树、图、排序查找算法',1),
                                                                                         ('CS23003','计算机网络',2,3.00,'2025-2026-2',45,'TCP/IP协议，HTTP，网络分层原理',1);

-- ==============================
-- 7.course_schedule 排课记录
-- ==============================
INSERT INTO course_schedule(course_id,teacher_id,class_id,week,day,lesson,room,color,status) VALUES
                                                                                                 (1,2,1,3,1,2,'A102','#409EFF',1),
                                                                                                 (1,2,1,3,3,4,'A102','#409EFF',1),
                                                                                                 (2,3,1,3,2,1,'B203','#67C23A',1),
                                                                                                 (3,2,2,3,4,3,'C305','#E6A23C',1);

-- ==============================
-- 8.selection_config 选课配置
-- ==============================
INSERT INTO selection_config(start_time,end_time,min_credit,max_credit,allowed_majors,status) VALUES
    ('2026-08-01 08:00:00','2026-08-20 23:59:59',2.00,10.00,'[1,2]',1);

-- ==============================
-- 9.selection_record 学生选课记录
-- ==============================
INSERT INTO selection_record(student_id,course_id,selected_time,status) VALUES
                                                                            (4,1,'2026-08-02 10:12:33',1),
                                                                            (4,2,'2026-08-02 10:14:11',1),
                                                                            (5,1,'2026-08-02 11:05:22',1),
                                                                            (5,3,'2026-08-02 11:08:45',1),
                                                                            (6,2,'2026-08-03 09:33:10',1);

-- ==============================
-- 10.student_score 成绩
-- ==============================
INSERT INTO student_score(course_id,student_id,teacher_id,score,usual_score,final_score,remark,status) VALUES
                                                                                                           (1,4,2,82.50,85.00,80.00,'平时出勤良好',1),
                                                                                                           (2,4,3,76.00,74.00,78.00,'基础有待加强',1),
                                                                                                           (1,5,2,88.00,90.00,86.00,'表现优秀',1),
                                                                                                           (3,5,2,79.50,77.00,82.00,'',1),
                                                                                                           (2,6,3,68.00,65.00,71.00,'需要多刷题练习',1);

-- ==============================
-- 11.attendance_session 签到会话
-- ==============================
INSERT INTO attendance_session(course_id,teacher_id,class_id,session_date,start_time,end_time,check_code,status) VALUES
                                                                                                                     (1,2,1,'2026-08-12','08:00:00','09:40:00','8A72K9',0),
                                                                                                                     (2,3,1,'2026-08-13','10:00:00','11:40:00','B3D5Q7',0);

-- ==============================
-- 12.attendance_record 签到记录
-- ==============================
INSERT INTO attendance_record(session_id,student_id,longitude,latitude,checkin_time,status) VALUES
                                                                                                (1,4,113.345210,23.124560,'2026-08-12 08:04:22',1),
                                                                                                (1,5,113.345230,23.124580,'2026-08-12 08:06:10',1),
                                                                                                (2,4,113.345205,23.124555,'2026-08-13 10:02:33',1),
                                                                                                (2,6,113.345222,23.124571,'2026-08-13 10:03:41',1);

-- ==============================
-- 13.notice 公告
-- ==============================
INSERT INTO notice(title,content,publisher_id,is_top,status) VALUES
                                                                 ('2026秋季选课通知','各位同学：新学期选课已开放，请在规定时间完成选课，注意学分上下限。',1,1,1),
                                                                 ('关于期末考试安排','2025-2026第二学期期末考试将于9月初进行，请同学们做好复习。',1,0,1);

-- ==============================
-- 14.notice_read_record 已读记录
-- ==============================
INSERT INTO notice_read_record(notice_id,user_id,read_time) VALUES
                                                                (1,4,'2026-08-02 14:22:10'),
                                                                (1,5,'2026-08-02 15:10:33'),
                                                                (1,6,'2026-08-03 08:44:21'),
                                                                (2,4,'2026-08-05 09:11:02');

-- ==============================
-- 15.qa_question 问答问题
-- ==============================
INSERT INTO qa_question(user_id,title,content,tags,is_top,like_count,reply_count,status) VALUES
                                                                                             (4,'Java SpringBoot如何配置MyBatis-Plus?','我在项目中集成MyBatis-Plus，Mapper扫描一直报错，请问正确配置步骤？','SpringBoot,MyBatis-Plus,后端',0,12,2,1),
                                                                                             (5,'数据结构快速排序不稳定怎么理解？','快速排序为什么是不稳定排序，举例子说明。','算法,数据结构',0,7,1,1);

-- ==============================
-- 16.qa_reply 问答回复
-- ==============================
INSERT INTO qa_reply(question_id,user_id,content,like_count,status) VALUES
                                                                        (1,2,'需要在启动类添加@MapperScan指定mapper包路径，同时检查yml数据库配置。',8,1),
                                                                        (1,3,'另外确认mybatis-plus版本与SpringBoot版本适配。',3,1),
                                                                        (2,3,'相同值元素经过交换后相对位置改变即为不稳定排序。',4,1);

-- ==============================
-- 17.ai_session AI会话
-- ==============================
INSERT INTO ai_session(user_id,title,model_name) VALUES
                                                     (4,'Java作业问题','gpt-4o-mini'),
                                                     (5,'算法题思路分析','gpt-4o-mini');

-- ==============================
-- 18.ai_message AI消息（已修正）
-- ==============================
INSERT INTO ai_message(session_id,sender,content) VALUES
                                                      (1,0,'帮我写一个SpringBoot简单CRUD示例'),
                                                      (1,1,'下面为你给出基于SpringBoot+MyBatis-Plus简单CRUD示例代码...'),
                                                      (2,0,'怎么优化冒泡排序时间复杂度？'),
                                                      (2,1,'冒泡排序可以通过标记是否发生交换做提前终止优化...');

-- ==============================
-- 19.course_evaluation 课程评价
-- ==============================
INSERT INTO course_evaluation(course_id,teacher_id,student_id,score,content) VALUES
                                                                                 (1,2,4,4.75,'张老师讲课条理清晰，实操案例较多，收获很大。'),
                                                                                 (2,3,4,4.20,'算法课程难度偏高，希望多增加习题讲解。'),
                                                                                 (1,2,5,4.80,'课堂节奏合适，作业量合理。');

-- ==============================
-- 20.warning_record 学业预警（已修正）
-- ==============================
INSERT INTO warning_record(user_id,warning_type,level,title,content,status) VALUES
    (6,'score',2,'课程成绩预警','该学生多门课程分数接近及格线，建议跟进学习情况。',1);

-- ==============================
-- 21.dashboard_stat 仪表盘统计样例
-- ==============================
INSERT INTO dashboard_stat(stat_type,target_date,target_id,value) VALUES
                                                                      ('user_count','2026-08-15',NULL,'{"admin":1,"teacher":2,"student":3}'),
                                                                      ('course_count','2026-08-15',NULL,'{"total":3,"open":3}');



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

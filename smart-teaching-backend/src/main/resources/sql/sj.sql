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
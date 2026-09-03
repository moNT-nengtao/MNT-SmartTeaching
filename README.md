# MNT-SmartTeaching 轻量化智能教学管理系统

> 本科毕业设计项目 · 单体 Web 架构 · Spring Boot + Vue3
> 仓库地址：https://github.com/moNT-nengtao/MNT-SmartTeaching.git

## 一、项目简介

MNT-SmartTeaching 是一套面向高校小型教学场景的轻量化智能教学管理系统。针对传统教务系统功能臃肿、架构冗余、交互老旧、缺乏个性化与智能化能力的痛点，系统摒弃冗余老旧模块，聚焦师生核心教学场景，以轻量化单体架构为基础，融合**八大智能化创新特色功能**，为管理员、教师、学生三类用户提供体验优质、特色鲜明的教学管理平台。

核心设计理念：

- **架构轻量化**：SpringBoot + Vue3 单体架构，无微服务、无集群，部署便捷、运行低耗，适配本科毕设研发标准
- **功能精细化**：保留教学核心刚需功能，剔除冗余模块，教务流程标准化
- **场景智能化**：课表、答疑、考勤、预警、选课推荐、AI 助教等八项差异化创新能力
- **数据可视化**：基于 ECharts 的多角色、多维度教学数据统计分析
- **终端范围**：仅支持 PC 浏览器端访问，不扩展小程序 / 移动端

## 二、核心功能

### 2.1 基础教务功能

| 模块 | 说明 | 主要角色 |
|---|---|---|
| 用户权限管理 | 账号登录认证、角色分配、启用禁用、批量导入，JWT 无状态鉴权 | 管理员 |
| 学院专业班级管理 | 三级组织架构维护，级联删除校验，批量导入导出 | 管理员 |
| 手动排课 | 人工辅助排课，冲突校验、批量排课、课表导出、排课记录 | 管理员 |
| 选课管理 | 选课时间/范围/名额配置，学生选课退课，超员锁定，热门预警 | 管理员/学生 |
| 成绩管理 | 平时/期末/综合成绩录入、批量导入、导出、异常筛查 | 教师/管理员/学生 |
| 公告通知管理 | 全校/课程级公告发布、编辑、撤回、置顶、已读标记 | 管理员/教师 |

### 2.2 八大创新特色功能

| 创新功能 | 核心能力 |
|---|---|
| 个性化智能课表 | 日历式周课表、课程颜色自定义、上课倒计时、课前 5 分钟浏览器提醒、课程收藏与备忘 |
| 匿名课程答疑社区 | 按课程分区、匿名/实名提问、教师置顶回复、点赞、标签归类检索 |
| 多角色数据可视化仪表盘 | 管理员全局统计、教师授课数据、学生成绩雷达图与绩点趋势、月度考勤统计 |
| 轻量化 AI 智能助教 | 基于 Ollama + Qwen3 的课程答疑、作业评语自动生成、学业分析与建议、每日次数限制 |
| 智能选课推荐 | 基于学业数据的个性化选修推荐、热门拥挤预警、剩余名额实时刷新、往届评价预览 |
| 动态验证码考勤 | 教师生成 6 位限时动态签到码、学生验证码签到、超时自动关通道、考勤统计报表 |
| 智能学业预警 | 定时扫描旷课/挂科/作业未提交，自动触发分级预警并推送，生成预警报告 |
| 课程与教师评价体系 | 结课匿名多维评分、教师评分榜单、按学院/学科筛选排名、评价统计分析 |

### 2.3 三角色职责

- **管理员**：系统最高权限，负责组织架构、用户账号、课程与排课、选课规则、全局数据监控与统计
- **教师**：授课、生成签到码考勤、成绩录入、答疑回复、作业发布批改、查看授课数据与评价、接收学业预警
- **学生**：选课、查看个性化课表、参与答疑、查询成绩与学业数据、作业提交、课程评价、接收学业预警

## 三、技术栈

### 后端（smart-teaching-backend）

| 分类 | 选型 |
|---|---|
| 语言 / 框架 | Java 17 · Spring Boot 4.1.0 |
| 数据持久化 | MyBatis-Plus 3.5.17（mybatis-plus-spring-boot4-starter + jsqlparser）· MySQL |
| 缓存 | Spring Data Redis（Lettuce + commons-pool2 连接池） |
| 认证授权 | Spring Security + JWT（jjwt 0.12.6，无状态，登出 Token 进 Redis 黑名单） |
| 定时任务 | Spring @Scheduled（学业预警扫描、考勤会话管理等） |
| AI 集成 | Ollama 本地模型（默认 qwen3:8b，WebClient 调用，每日次数限制） |
| 接口文档 | springdoc-openapi 2.6.0（Swagger UI） |
| 其他 | Fastjson2 2.0.53 · Hutool 5.8.30 · EasyExcel 3.3.2（导入导出）· Hibernate Validator · Lombok · WebFlux |

### 前端（smart-teaching-web）

| 分类 | 选型 |
|---|---|
| 框架 | Vue 3.4 · Vite 5.2 · Vue Router 4.3（按角色动态路由） |
| UI 组件 | Element Plus 2.6 · sass |
| 状态管理 | Pinia 2.1 |
| 数据可视化 | ECharts 5.5 |
| HTTP | Axios（统一封装，请求拦截注入 JWT，响应拦截统一错误处理） |
| 其他 | xlsx（表格导入导出） |

### 部署

Nginx 1.31.3 本地部署，监听 8081，托管前端 `dist` 静态资源并反向代理 `/api` 至后端 8080。

## 四、系统架构

```text
┌─────────────────────────────────────────────────────────────┐
│                       浏览器（PC Web）                        │
│   Vue3 + Element Plus 单页应用  /  http://localhost:8081      │
└──────────────────────────┬──────────────────────────────────┘
                           │  /api/**  反向代理
┌──────────────────────────▼──────────────────────────────────┐
│                        Nginx 1.31.3                         │
│    静态资源 dist 托管 · /avatar /files 文件映射 · 静态缓存      │
└──────────────────────────┬──────────────────────────────────┘
                           │  http://localhost:8080
┌──────────────────────────▼──────────────────────────────────┐
│                 Spring Boot 后端（单体应用）                   │
│                                                             │
│   Controller 层 → Service 层 → Mapper 层（MyBatis-Plus/XML）  │
│        ↑  JWT 过滤器校验（SecurityContext）                   │
│   common（Result/DTO/VO/异常/工具） · config（安全/Redis/      │
│   MyBatis/Swagger/Ollama/WebMvc） · 定时任务                   │
└───────┬──────────────────┬───────────────────┬──────────────┘
        │                  │                   │
        ▼                  ▼                   ▼
     MySQL              Redis               Ollama
  smart_teaching    缓存 / Token 黑名单     qwen3:8b（AI 助教）
```

请求链路：前端 axios 携带 `Authorization: Bearer <token>` → Nginx `/api` 反代 → `JwtAuthenticationFilter` 校验 Token（含 Redis 黑名单校验）→ 鉴权通过后进入 Controller → Service 业务处理 → Mapper 数据持久化，统一返回 `{ code, data, msg }`（成功 `code=1`）。

安全约束：`/api/auth/login`、`/api/auth/logout`、`/api/register`、`/api/captcha` 放行；`/api/admin/**` 需 ADMIN 角色；其余接口一律要求登录；`OPTIONS` 预检放行；跨域已全局配置。

## 五、项目目录结构

```text
MNT-SmartTeaching/
├─ mnt-st/                                  # 主工程
│  ├─ smart-teaching-backend/               # 后端 Spring Boot 工程
│  ├─ smart-teaching-web/                   # 前端 Vue3 工程（源码）
│  ├─ nginx-1.31.3/                         # 本地 Nginx 部署目录（含 dist 与部署配置）
│  ├─ .github/modernize/java-upgrade/       # GitHub 现代化改造钩子脚本
│  ├─ .vscode/                              # 编辑器配置
│  └─ .gitignore
└─ 项目文档/                                 # 项目文档目录（需求/接口/结构说明等）
   └─ README.md                             # 本文档
```

### 后端结构（com.smartteaching）

```text
smart-teaching-backend/
├─ SmartTeachingApplication.java        # 启动类（@EnableScheduling）
├─ common/                              # 基础能力
│  ├─ constant/                         # 常量与枚举（ResultCode/UserRoleEnum/考勤状态等）
│  ├─ dto/                              # 请求/入参 DTO（按业务域分包）
│  ├─ vo/                               # 响应 VO（按业务域分包）
│  ├─ exception/                        # 全局异常与处理器
│  ├─ result/                           # Result / PageResult 统一返回
│  └─ utils/                            # JwtUtil/RedisUtils/SecurityUtils/Excel 工具等
├─ config/                              # 配置类
│  ├─ SecurityConfig.java               # Spring Security 过滤链 + 跨域
│  ├─ JwtAuthenticationFilter.java      # JWT 解析过滤器（含 Redis 黑名单）
│  ├─ RedisConfig / MybatisPlusConfig / WebMvcConfig / SwaggerConfig
│  ├─ OllamaConfig.java                 # AI 助教接入配置
│  ├─ WebClientConfig.java              # WebClient Bean
│  └─ AttendanceScheduleTask.java       # 定时任务
├─ controller/                          # REST 接口（15 个业务域）
│  └─ auth/ course/ selection/ score/ attendance/ notice/ qa/ ai/
│     evaluation/ warning/ dashboard/ org/ user/ schedule/ homework/
├─ entity/                              # 数据库实体（24 张表对应）
├─ mapper/                              # MyBatis-Plus Mapper 接口
├─ service/                             # 业务接口 + 实现
├─ resources/
│  ├─ application.yml / -dev / -prod    # 多环境配置
│  ├─ mapper/*.xml                      # SQL 映射
│  └─ sql/smart_teaching_mysql.sql      # 建库脚本（含初始化数据）
├─ uploads/                             # 上传文件（头像/作业附件，运行时生成）
└─ src/test/                            # 单元测试（Auth/Dashboard 等）
```

### 前端结构（src）

```text
smart-teaching-web/src/
├─ api/            # 接口请求层（15 个业务域 + 统一 request 封装）
├─ views/          # 页面视图（按业务模块分目录，三角色各自视图）
│  └─ login/ dashboard/ user/ org/ course/ selection/ score/
│     notice/ homework/ schedule/ qa/ ai/ attendance/ warning/ evaluation/
├─ router/index.js # 路由表 + 全局守卫（按角色动态加载路由）
├─ store/          # Pinia（user：token/角色/权限；app：布局状态）
├─ components/     # 公共组件（ChartCard/Pagination/SearchForm）
├─ utils/          # request/auth/permission/format 工具
├─ assets/         # 静态资源
├─ App.vue / main.js
└─ .env.development / .env.production   # 环境变量（VITE_APP_BASE_API=/api）
```

## 六、数据库设计

MySQL 数据库 `smart_teaching`，共 **24 张表**，脚本位于 `smart-teaching-backend/src/main/resources/sql/smart_teaching_mysql.sql`（含初始化数据）。

| 分组 | 表 |
|---|---|
| 用户权限 | sys_user、sys_role |
| 组织架构 | org_college、org_major、org_class |
| 教务核心 | course、course_schedule、selection_config、selection_record、student_score |
| 考勤 | attendance_session、attendance_record |
| 公告 | notice、notice_read_record |
| 答疑 | qa_question、qa_reply |
| AI | ai_session、ai_message、ai_daily_usage |
| 作业 | homework、homework_submission |
| 评价 / 预警 / 统计 | course_evaluation、warning_record、dashboard_stat |

## 七、快速开始

### 7.1 环境要求

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 后端运行 |
| Maven | 3.8+ | 后端构建（工程内含 mvnw） |
| Node.js | 18+ | 前端构建 |
| MySQL | 5.7 / 8.0 | 数据库 |
| Redis | 5.0+ | 缓存 / Token 黑名单（无 Redis 可注释 application.yml 中 redis 配置块） |
| Ollama（可选） | 最新 | AI 助教，需拉取 qwen3:8b 模型 |
| Nginx | 1.31.3 | 本地部署（工程已内置） |

### 7.2 初始化数据库

```sql
-- 方式一：命令行导入
mysql -uroot -p < smart-teaching-backend/src/main/resources/sql/smart_teaching_mysql.sql

-- 方式二：登录后执行
mysql -uroot -p
source smart-teaching-backend/src/main/resources/sql/smart_teaching_mysql.sql;
```

默认连接配置：`jdbc:mysql://localhost:3306/smart_teaching`，用户名 `root`，密码 `123456`（按需修改 `application.yml` / `application-dev.yml`）。

### 7.3 启动后端

```bash
cd mnt-st/smart-teaching-backend
# 方式一：Maven 直接启动
mvn spring-boot:run
# 方式二：打包后启动
mvn clean package -DskipTests
java -jar target/smart-teaching-backend-0.0.1-SNAPSHOT.jar
```

- 服务端口：`8080`
- 接口前缀：`/api`（Controller 直接以 `/api` 为映射前缀）
- 接口文档：启动后访问 `http://localhost:8080/swagger-ui/index.html`（springdoc 自动生成）
- 上传目录：`./uploads`（头像在 `uploads/avatars`，作业附件在 `uploads/`）

### 7.4 启动前端（开发模式）

```bash
cd mnt-st/smart-teaching-web
npm install
npm run dev
```

- 访问地址：`http://localhost:5173`（自动打开）
- 开发代理：`/api`、`/files` 已代理到 `http://localhost:8080`，无需处理跨域

### 7.5 生产部署（Nginx）

方式一：执行一键部署脚本（Windows）

```bat
cd mnt-st/smart-teaching-web
build.bat
```

脚本自动完成 4 步：关闭 Nginx → 删除 dist → `npm run build` → 启动 Nginx。

方式二：手动部署

```bash
cd mnt-st/smart-teaching-web
npm run build          # 产物输出到 dist/
# 将 dist/ 拷贝至 nginx-1.31.3/smart-teaching-web/dist
# 配置 nginx-1.31.3/conf/nginx.conf（已就绪）
# 启动 Nginx
nginx-1.31.3/nginx.exe
```

Nginx 关键配置（已内置）：

```nginx
listen 8081;
root ./smart-teaching-web/dist;                      # 前端静态资源
location /api/    { proxy_pass http://localhost:8080; }  # 接口反向代理
location ~ ^/avatar/  { alias ../smart-teaching-backend/uploads/avatars/; }  # 头像
location ~ ^/files/   { alias ../smart-teaching-backend/uploads/; }           # 通用文件
location /       { try_files $uri $uri/ /index.html; }  # history 路由回退
```

部署后访问：`http://localhost:8081`

## 八、默认账号

| 角色 | 账号 | 密码 | 说明 |
|---|---|---|---|
| 系统管理员 | admin | 123456 | 最高权限 |
| 教师 | t001 | 123456 | 张教授 |
| 教师 | t002 | 123456 | 李老师 |
| 学生 | s230101（如 s230102、s230201…） | 123456 | 莫能涛，其余学生账号见建库脚本 sys_user 表 |

说明：密码为 MD5 存储（`123456` 的密文 `e10adc3949ba59abbe56e057f20f883e`），登录时按 MD5 比对。

## 九、接口约定

- 统一前缀：`/api`
- 统一返回：`{ "code": 1, "data": {}, "msg": "success" }`，成功 `code=1`
- 认证方式：请求头 `Authorization: Bearer <token>`
- 鉴权失败：`401` 未登录 / Token 过期，`403` 无权限
- 接口定义文档：
  - 需求侧接口文档：`项目文档/接口文档.md`、`项目文档/前后端接口文档.md`
  - OpenAPI 导入文件：`项目文档/智慧教学平台.openapi.json`（可导入 Apifox）
  - 作业/预警接口：`项目文档/homework_warning_api.json`、`smart-teaching-backend/docs/attendance-api.json`
  - 在线 Swagger：后端启动后访问 `http://localhost:8080/swagger-ui/index.html`

## 十、项目文档索引

| 文档 | 路径 | 说明 |
|---|---|---|
| 需求规格说明 | `项目文档/MNT-SmartTeaching轻量化智能教学管理系统需求规格说明文档.docx` | 权威需求定义（背景/功能/技术约束/实施计划） |
| 接口文档 | `项目文档/接口文档.md` | 前端真实需要的接口与字段（后端开发遵守版） |
| 前后端接口文档 | `项目文档/前后端接口文档.md` | 接口联调说明 |
| 前端目录结构设计 | `项目文档/前端目录结构设计.md` | 前端目录、组件、依赖链、数据流 |
| 后端结构说明 | `项目文档/后端结构说明.md` | 后端分层架构与职责边界 |
| 后端构建流程 | `项目文档/后端构建流程.md` | 后端分步骤搭建指南 |
| 数据库表结构预览 | `项目文档/数据库表结构预览.txt` | 数据表清单 |
| 数据库脚本 | `项目文档/smart_teaching_mysql.sql`（另存于后端 resources/sql 下） | 建库与初始化数据 |
| OpenAPI 文件 | `项目文档/智慧教学平台.openapi.json` 等 | 可导入 Apifox 调试 |

## 十一、开发与版本规划

### 实施阶段

1. **基础搭建**：环境配置、单体架构、技术栈整合、数据库设计、用户权限模块
2. **基础教务**：学院班级、手动排课、选课、成绩、公告模块
3. **创新功能 P1**：个性化课表、动态验证码考勤、数据可视化仪表盘、匿名答疑社区
4. **创新功能 P2**：AI 智能助教、智能选课推荐、智能学业预警、课程评价体系
5. **优化迭代**：UI/交互优化、统计报表优化、稳定性调试

### Git 分支

当前主分支 `main`，提交按模块演进（仪表盘 → 选课 → 学业预警 → 课程管理 → 答疑社区 → 公告 → 数据库优化等）。

## 十二、常见问题

- **前端页面刷新 404**：确认 Nginx 已配置 `try_files $uri $uri/ /index.html`（history 路由必需）
- **登录成功但接口 401**：确认 Token 未过期、Redis 无该 Token 黑名单记录
- **AI 助教无响应**：确认 Ollama 已启动并拉取 qwen3:8b，且 `application.yml` 的 `ollama.host` 可达
- **上传头像/附件无法访问**：确认 `uploads` 目录存在且 Nginx `/avatar`、`/files` 映射路径正确
- **接口跨域报错**：开发环境走 Vite 代理；生产环境统一经 Nginx 反代，避免直接跨域调用

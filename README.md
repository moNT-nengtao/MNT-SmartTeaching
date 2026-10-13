# MNT-SmartTeaching 智能教学管理系统

个人毕业设计 · Spring Cloud 微服务 · Spring Boot 4 + Vue3
仓库：https://github.com/moNT-nengtao/MNT-SmartTeaching.git

## 项目简介

高校轻量化教务管理系统：8 个业务微服务 + 1 网关，覆盖用户权限、课程排课、选课成绩、考勤签到、学业预警、AI 助教、数据仪表盘、作业通知，管理员 / 教师 / 学生三角色统一入口。

## 系统亮点

- **架构解耦**：按业务域拆 8 个服务独立部署，Nacos 注册发现、Gateway 前缀路由、OpenFeign 契约调用并携带内部标识防越权；单模块改动不影响其他模块，分库演进已预留扩展点。
- **安全体系**：Spring Security + JWT 三角色权限，登出令牌进 Redis 黑名单立即失效；网关与各服务双重过滤，内部接口由专用过滤器隔离外部访问。
- **业务智能化**：AI 助教接入 Ollama 本地大模型（答疑 / 作业评语 / 学业分析，异步不阻塞、每日限额、会话可追溯）；考勤九宫格图案签到，缺勤自动落实旷课并联动三级学业预警；选课基于学业数据个性化推荐；排课内置班级 / 教师 / 教室三重冲突校验；成绩支持异常筛查与批量导入导出。
- **数据可视化**：ECharts 三角色仪表盘——管理员看全局统计与选课率，教师看授课评分与考勤趋势，学生看 GPA、挂科雷达与成绩趋势。
- **功能闭环**：匿名答疑社区、五维课程评价、作业批改流转、公告通知。

## 技术架构

```
Browser
   │  http://localhost:5173 (Vue3 + Vite)
   ▼
Gateway  :8080  API 网关（前缀路由 / JWT 鉴权 / CORS）
   │  Nacos 注册中心 :9848
   ├── uaa-service             :8081  认证 / 用户 / 组织
   ├── course-service          :8082  课程 / 排课
   ├── selection-score-service :8083  选课 / 成绩
   ├── attendance-service      :8084  考勤签到
   ├── warning-service         :8085  学业预警
   ├── ai-service              :8086  AI 助教（Ollama）
   ├── analysis-service        :8087  数据仪表盘
   └── teaching-service        :8088  作业 / 通知 / 评价 / 答疑
              │  OpenFeign 契约调用（内部标识防越权）
              ▼
        MySQL（共享单库过渡） + Redis（缓存 / 黑名单 / 限额）
```

## 技术栈

| 分类 | 选型 |
|---|---|
| 语言 / 框架 | Java 17 · Spring Boot 4.0.8 · Spring Cloud 2025.1.0 |
| 微服务组件 | Spring Cloud Gateway · Nacos 3.2.4 · OpenFeign |
| 数据持久化 | MyBatis-Plus 3.5.17 · MySQL 8 |
| 缓存 | Spring Data Redis（Lettuce + commons-pool2） |
| 认证授权 | Spring Security + JWT（登出令牌进 Redis 黑名单） |
| AI 能力 | Ollama + Qwen3 本地大模型 |
| 前端 | Vue3 · Vite · Element Plus · ECharts · Axios |
| 部署 | Nginx 反向代理 · Windows 服务（MySQL / Redis） |

## 快速开始

### 0. 环境准备

| 组件 | 版本 | 检查方式 |
|---|---|---|
| JDK | 17+ | `java -version` |
| Node.js | 18+ | `node -v` |
| MySQL | 8.x | 系统服务 `MySQL80`，账号 root/123456 |
| Redis | 任意 | 系统服务 `Redis`，密码 123456 |
| Nacos | 3.2.4 | 解压到仓库同级目录 `nacos-server-3.2.4/`（不入库） |

一键启动脚本会自动检查以上环境，缺什么直接提示，不会空跑。

### 1. 初始化数据库

```bash
mysql -uroot -p123456 < sql/smart_teaching_mysql.sql
```

### 2. 构建后端

```bash
cd smart-teaching-cloud
mvn clean package -DskipTests
```

### 3. 安装前端依赖

```bash
cd smart-teaching-web
npm install
```

### 4. 一键启动

双击仓库根目录 `start-all.cmd`，静默启动、日志落盘、后台运行：

```
MySQL → Redis → Nacos → 9 个微服务 → 前端 Vite
```

启动完成后自动汇总各端口状态（OK / DOWN）。

### 5. 验证

| 入口 | 地址 | 说明 |
|---|---|---|
| 前端页面 | http://localhost:5173 | Vite 开发模式，/api 已代理到网关 |
| 网关接口 | http://localhost:8080/api/xxx | 后端接口统一入口 |
| Nacos 控制台 | http://localhost:8089/nacos | 账号 nacos / nacos |

登录测试账号 `admin / 123456`（管理员），教师、学生账号由管理员在系统内创建。

### 6. 停止

双击 `stop-all.cmd`，停止 Nacos、微服务与前端（MySQL / Redis 保留运行）。

### 7. 常见问题

- **服务全部 DOWN**：检查 MySQL / Redis 是否运行、Nacos 是否就绪，日志在 `smart-teaching-cloud/<模块>/target/*.log`
- **前端 404 / 接口不通**：确认网关 8080 已启动，前端 /api 代理指向网关
- **端口被占用**：服务端口固定 8080-8088，释放占用或修改服务配置
- **Nacos 启动卡住**：确认已解压 nacos-server-3.2.4 且 JDK 匹配，日志见 `nacos-server-3.2.4/nacos-start.log`

## 目录结构

```
MNT-SmartTeaching/
├── smart-teaching-cloud/       # 后端微服务工程（11 个 Maven 模块）
│   ├── common/                 # 公共模块（工具/异常/公共配置/公共 Mapper）
│   ├── api/                    # OpenFeign 接口契约
│   ├── gateway-service/        # 网关
│   └── *-service/              # 8 个业务服务
├── smart-teaching-web/         # 前端 Vue3 工程
├── sql/                        # 数据库初始化脚本
├── start-all.cmd / .ps1        # 一键启动（含环境自检）
├── stop-all.cmd / .ps1         # 一键停止
└── README.md
```

## 文档

需求规格说明、接口文档、后端结构、微服务改造说明、SpringCloud 学习讲义等完整文档维护在本地 `项目文档/` 目录，不随仓库同步。

## 注意事项

- Nacos、Nginx 为本地运行环境，不入库，克隆后需自行准备
- 单体版（smart-teaching-backend）为历史架构存档，仅保留本地
- 数据库当前为多服务共享单库过渡，按域分库演进已在架构中预留

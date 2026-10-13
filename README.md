# MNT-SmartTeaching 智能教学管理系统

个人毕业设计 · Spring Cloud 微服务 · Spring Boot 4 + Vue3
仓库：https://github.com/moNT-nengtao/MNT-SmartTeaching.git

## 项目简介

高校轻量化教务管理系统：8 个业务微服务 + 1 网关，覆盖用户权限、课程排课、选课成绩、考勤签到、学业预警、AI 助教、数据仪表盘、作业通知，管理员 / 教师 / 学生三角色统一入口。

## 系统亮点

- **微服务架构**：按业务域拆 8 个服务独立部署，Nacos 注册发现、Gateway 统一入口按前缀路由、OpenFeign 契约调用并携带内部标识防越权，服务间不直连对方数据库。
- **统一安全体系**：Spring Security + JWT 三角色权限，登出令牌进 Redis 黑名单立即失效；网关与各服务双重过滤，内部接口由专用过滤器隔离外部访问。
- **AI 助教**：接入 Ollama 本地大模型，支持知识点答疑、作业评语生成、学业分析，异步调用不阻塞主流程、每日限额防滥用、历史会话可追溯。
- **业务智能化**：考勤九宫格图案签到，缺勤自动落实旷课并联动三级学业预警；选课基于学业数据个性化推荐；排课内置班级 / 教师 / 教室三重冲突校验；成绩支持异常筛查与批量导入导出。
- **数据可视化**：ECharts 三角色仪表盘——管理员全局统计与选课率、教师授课评分与考勤趋势、学生 GPA 挂科雷达与成绩趋势。
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
        MySQL（多服务共享单库） + Redis（缓存 / 黑名单 / 限额）
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

### 4. 一键启动（开发模式）

双击 `scripts/start-all.cmd`，静默启动、日志落盘、后台运行：

```
MySQL → Redis → Nacos → 9 个微服务 → 前端 Vite
```

启动完成后自动汇总各端口状态（OK / DOWN）。日常开发改代码即时生效，页面入口 http://localhost:5173。

### 5. 验证（开发模式）

| 入口 | 地址 | 说明 |
|---|---|---|
| 前端页面 | http://localhost:5173 | Vite 开发模式，/api 已代理到网关 |
| 网关接口 | http://localhost:8080/api/xxx | 后端接口统一入口 |
| Nacos 控制台 | http://localhost:8089/nacos | 账号 nacos / nacos |

登录测试账号 `admin / 123456`（管理员），教师、学生账号由管理员在系统内创建。

### 6. 生产部署（Nginx）

构建前端并通过 Nginx 托管，贴近真实部署：双击 `scripts/start-prod.cmd`，自动完成 环境自检 → 构建前端 → 生成 Nginx 配置 → 启动 Nginx → 校验 ：

```
页面入口  http://localhost:8888   （Nginx 托管 dist，/api 反向代理到网关 8080）
停止部署  scripts/stop-prod.cmd
```

- 依赖后端已运行（网关 8080 未启动会提示先跑 `start-all.cmd`）；前端改动需重新 `start-prod.cmd`（或 `scripts/build-web.cmd` 单独构建）
- 默认端口 8888（80 常被占用）；如需更换，改 `scripts/start-prod.ps1` 顶部 `$Port`
- 项目位于中文路径时，脚本自动在 `C:\Users\Public` 建立 junction 映射，以纯 ASCII 路径运行 Nginx（Windows 版 Nginx 无法直接处理中文路径）

### 7. 停止

双击 `scripts/stop-all.cmd`，停止 Nacos、微服务与前端（MySQL / Redis 保留运行）；生产模式单独用 `scripts/stop-prod.cmd` 停止 Nginx。

### 8. 常见问题

- **服务全部 DOWN**：检查 MySQL / Redis 是否运行、Nacos 是否就绪，日志在 `smart-teaching-cloud/<模块>/target/*.log`
- **前端 404 / 接口不通**：确认网关 8080 已启动，前端 /api 代理指向网关
- **端口被占用**：服务端口固定 8080-8088，释放占用或修改服务配置；生产页面 8888 被占用则改 `start-prod.ps1` 顶部 `$Port`
- **Nacos 启动卡住**：确认已解压 nacos-server-3.2.4 且 JDK 匹配，日志见 `nacos-server-3.2.4/nacos-start.log`
- **生产模式页面打不开**：先跑 `start-all.cmd` 起后端，再跑 `start-prod.cmd`；Nginx 日志在 `nginx-1.31.3/logs/error.log`

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
├── scripts/                    # 全部脚本（公共定义统一在 lib\common.ps1）
│   ├── start-all / stop-all    # 开发模式一键启动 / 停止（Nacos + 9 服务 + Vite + Nginx）
│   ├── start-prod / stop-prod  # 生产模式启动 / 停止（Nginx 托管 + 反向代理）
│   ├── build-web               # 单独构建前端 dist
│   ├── lib/common.ps1          # 路径 / 端口 / 工具函数（各脚本共用）
│   └── nginx/                  # Nginx 生产配置模板
└── README.md
```

## 注意事项

- Nacos、Nginx 为本地运行环境，不入库，克隆后需自行准备
- 数据库为多服务共享单库，跨服务数据访问统一经 OpenFeign 契约，不直连其他服务表

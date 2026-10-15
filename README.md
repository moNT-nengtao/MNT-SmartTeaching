<div align="center">

# MNT · SmartTeaching

### 高校智能教学管理系统 —— Spring Cloud + Vue3 微服务实践

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.x-green)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.x-6DB33F)
![Vue](https://img.shields.io/badge/Vue-3.x-42b883)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.x-409EFF)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.x-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-lightblue)
![Redis](https://img.shields.io/badge/Redis-7.x-red)
![Nacos](https://img.shields.io/badge/Nacos-3.2.x-000000)

</div>

---

高校轻量化教务管理系统，覆盖管理员 / 教师 / 学生三角色完整业务闭环。仓库：https://github.com/moNT-nengtao/MNT-SmartTeaching.git

覆盖用户权限、课程排课、选课成绩、考勤签到、学业预警、AI 助教、数据仪表盘、作业通知等完整教务闭环，管理员 / 教师 / 学生三角色统一入口，各角色拥有独立工作台。

## 功能总览

| 角色 | 核心功能 |
|---|---|
| 管理员 | 管理驾驶舱、用户 / 组织管理、课程排课、选课管理、作业管理、成绩统计、教师评分榜、通知公告、答疑社区、AI 助教、学业预警 |
| 教师 | 教师工作台、作业管理、成绩录入、九宫格课堂签到、教学评价、通知公告、答疑社区、AI 助教、学业预警 |
| 学生 | 学业中心、我的课表、选课大厅 / 我的选课、成绩查询、我的作业、九宫格签到、课程评价、通知公告、答疑社区、AI 助教、学业预警 |

以下均为系统真实运行画面，按技术亮点挑选展示。

## 核心业务与技术展示

### 数据可视化 · 管理驾驶舱

管理员入口即全局数据看板：班级 / 教师 / 学生 / 课程总数、选课率、考勤合格率一屏概览，各学院学生分布、师生比例、近 7 日系统活跃度由 ECharts 实时渲染。

![管理驾驶舱](docs/screenshots/02-dashboard.png)

### 九宫格图案签到 · 考勤防代签

考勤不是简单的"点一下签到"：教师滑动九宫格绘制随机图案生成签到会话，学生必须绘制相同图案才能签到成功，从机制上防止代签。考勤结果自动区分成功 / 迟到 / 请假 / 旷课。

教师端发起签到并实时查看考勤名单：

![课堂签到](docs/screenshots/17-attendance.png)

学生端同步绘制图案完成签到，考勤记录完整可追溯：

![我的考勤](docs/screenshots/22-attendance-student.png)

### 排课三重冲突校验

排课内置冲突检测：同一班级、同一教师、同一教室的时段冲突自动拦截，从源头避免"教师连堂撞车、教室一室多用"，支持手动排课与批量排课。

![排课管理](docs/screenshots/06-schedule.png)

### 成绩自动计算与异常筛查

教师按课程录入平时 / 期末成绩，综合成绩与等级自动计算，平均分、及格率实时统计；管理员端自动筛查异常成绩（如低于 60 分、缺考），支持导出，为教学质量监督提供依据。

![成绩录入](docs/screenshots/16-score-entry.png)

![成绩统计](docs/screenshots/09-score.png)

### 选课智能推荐

选课大厅除按课程卡片选课外，还提供「智能推荐」标签——基于学业数据为每位学生个性化推荐课程，推荐结果直接展示剩余名额与课程评分。

![选课大厅](docs/screenshots/20-selection-hall.png)

### 五维课程评价

学生从授课能力、课堂氛围、知识讲解、作业批改、答疑服务五个维度评价课程，教师端以雷达图呈现各维度评分，管理员端生成教师评分榜，前三名突出展示。

![教学评价](docs/screenshots/18-evaluation-teacher.png)

![教师评分榜](docs/screenshots/10-evaluation.png)

### AI 助教 · 本地大模型

AI 助教接入 Ollama 本地大模型，支持知识点答疑、作业评语生成、学业分析三种模式；异步调用不阻塞主流程，每日限额防滥用，历史会话可追溯。

![AI助教](docs/screenshots/13-ai.png)

### 学业预警 · 跨服务联动

预警并非孤立的统计页面：考勤缺勤自动落实旷课、成绩不及格自动触发挂科预警，旷课 / 挂科 / 作业未交三类预警跨服务联动生成，附带预警原因说明，支持导出报告。

![学业预警](docs/screenshots/14-warning.png)

### 学业数据分析 · 学生学业中心

学生入口即学业数据看板：当前 GPA、已修学分、挂科科目，配各科成绩雷达图、绩点趋势、月度考勤统计与各科成绩对比，学业状态一目了然。

![学业中心](docs/screenshots/19-student-dashboard.png)

## 技术架构

```
Browser
   │  http://localhost:5173 开发 (Vue3 + Vite) / http://localhost:8888 生产 (Nginx)
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

8 个业务微服务 + 1 网关按业务域独立部署：Nacos 注册发现、Gateway 统一入口按前缀路由、OpenFeign 契约调用并携带内部标识防越权，服务间不直连对方数据库。统一 Spring Security + JWT 三角色权限，登出令牌进 Redis 黑名单立即失效。考勤、成绩、预警模块间通过微服务调用自动联动（如缺勤自动落实旷课并触发预警），体现微服务解耦后的业务闭环。

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

登录测试账号 `admin / 123456`（管理员）、`t001 / 123456`（教师）、`s230101 / 123456`（学生）。

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
├── docs/screenshots/           # 功能演示截图（本 README 配图）
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
- 以上功能截图运行于生产模式（Nginx :8888），登录账号为系统预置测试数据

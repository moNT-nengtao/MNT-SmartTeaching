# MNT-SmartTeaching 智能教学管理系统

个人毕业设计项目（MNT）· Spring Cloud 微服务架构 · Spring Boot 4 + Vue3 + Nacos
仓库地址：https://github.com/moNT-nengtao/MNT-SmartTeaching.git

## 项目简介

面向高校轻量化教学场景的教务管理系统。按业务域拆分为 8 个微服务加 1 个网关，覆盖用户权限、课程排课、选课成绩、考勤签到、学业预警、AI 助教、数据分析和作业通知等完整教务链路，为管理员、教师、学生三角色提供统一的教学管理平台。

## 系统亮点

项目在单体架构的基础上，按企业级微服务实践做了完整的拆分与治理，核心亮点集中在三方面：架构解耦、安全体系与业务智能化。

**架构按业务域彻底拆分。** 用户认证、课程排课、选课成绩、考勤、预警、AI、数据分析、教学八个服务独立部署、独立演进，通过 Nacos 注册发现、Gateway 统一入口按前缀路由，服务间以 OpenFeign 契约调用并携带内部调用标识防止越权。一个前端请求只经过网关，内部各服务互不感知对方实现细节，改动单一模块不影响其他模块。数据库当前以共享单库过渡，按域分库演进已预留扩展点。

**安全体系无状态可失效。** Spring Security + JWT 三角色权限模型，登录签发令牌、登出令牌进 Redis 黑名单立即失效，热点数据走 Redis 缓存。权限控制在网关和各服务双重过滤，内部接口通过专用过滤器隔离外部访问。

**业务智能化是项目的主要特色。** AI 助教接入 Ollama 本地大模型，学生可按知识点答疑，教师可一键生成作业评语与学业分析，异步调用不阻塞主流程，每日次数限额防滥用，历史会话可追溯。考勤采用九宫格图案签到，学生按图案对照绘制，会话超时自动关闭通道，缺勤自动落实旷课并联动预警服务生成三级学业预警（旷课、挂科、作业未交三类），定时任务兜底保障。选课模块提供基于学业数据的个性化推荐，含推荐理由与课程热度标记。排课内置班级、教师、教室三重冲突校验，成绩模块支持异常筛查与批量导入导出。数据侧用 ECharts 为三角色构建可视化仪表盘，管理员看全局统计与选课率，教师看授课评分与考勤趋势，学生看 GPA、挂科雷达与成绩趋势。另有匿名答疑社区、五维课程评价、作业批改流转、公告通知等完整闭环。

## 技术架构

```
Browser
   │  http://localhost:5173 (Vue3 + Vite)
   ▼
Gateway  :8080  API 网关（前缀路由 / JWT 鉴权 / CORS）
   │  Nacos 注册中心 :9848
   ├── uaa-service            :8081  认证 / 用户 / 组织
   ├── course-service         :8082  课程 / 排课
   ├── selection-score-service :8083  选课 / 成绩
   ├── attendance-service     :8084  考勤签到
   ├── warning-service        :8085  学业预警
   ├── ai-service             :8086  AI 助教（Ollama）
   ├── analysis-service       :8087  数据仪表盘
   └── teaching-service       :8088  作业 / 通知 / 评价 / 答疑
              │  OpenFeign 契约调用（内部标识防越权）
              ▼
        MySQL（共享单库过渡） + Redis（缓存 / 黑名单 / 限额）
```

服务端口与职责见 [docs/SpringCloud微服务改造说明.md](docs/SpringCloud微服务改造说明.md)，接口定义见 [docs/接口文档.md](docs/接口文档.md)。

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

一键启动脚本启动前会自动检查以上环境，缺什么直接提示，不会空跑。

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

双击仓库根目录 `start-all.cmd`。脚本按以下顺序静默启动，全部后台运行、日志落盘，任务栏不会出现服务黑框：

```
MySQL → Redis → Nacos → 9 个微服务 → 前端 Vite
```

启动完成后自动汇总各端口状态（OK / DOWN）。

### 5. 验证

| 入口 | 地址 | 说明 |
|---|---|---|
| 前端页面 | http://localhost:5173 | Vite 开发模式，/api 已代理到网关 |
| 网关接口 | http://localhost:8080/api/xxx | 后端接口统一入口 |
| Nacos 控制台 | http://localhost:18081/nacos | 账号 nacos / nacos |

登录测试账号 `admin / 123456`（管理员），教师、学生账号由管理员在系统内创建。

### 6. 停止

双击仓库根目录 `stop-all.cmd`，停止 Nacos、微服务与前端（MySQL / Redis 保留运行）。

### 7. 常见问题

- **服务全部 DOWN**：检查 MySQL / Redis 是否运行，Nacos 是否就绪，各服务日志在 `smart-teaching-cloud/<模块>/target/*.log`
- **前端 404 / 接口不通**：确认网关 8080 已启动，前端 /api 代理指向网关
- **端口被占用**：服务端口固定（8080-8088），释放占用或修改对应服务配置
- **Nacos 启动卡住**：确认本机已解压 nacos-server-3.2.4 且 JDK 版本匹配，日志见 `nacos-server-3.2.4/nacos-start.log`

## 目录结构

```
MNT-SmartTeaching/
├── smart-teaching-cloud/       # 后端微服务工程（11 个 Maven 模块）
│   ├── common/                 # 公共模块
│   ├── api/                    # OpenFeign 接口契约
│   ├── gateway-service/        # 网关
│   └── *-service/              # 8 个业务服务
├── smart-teaching-web/         # 前端 Vue3 工程
├── sql/                        # 数据库初始化脚本
├── docs/                       # 项目文档
├── tools/                      # 开发辅助脚本
├── start-all.cmd / .ps1        # 一键启动（含环境自检）
├── stop-all.cmd / .ps1         # 一键停止
└── README.md
```

## 文档导航

| 文档 | 内容 |
|---|---|
| docs/SpringCloud微服务改造说明.md | 微服务拆分方案与改造过程 |
| docs/接口文档.md · docs/前后端接口文档.md | 接口定义与联调说明 |
| docs/后端结构说明.md · docs/后端构建流程.md | 后端分层与构建 |
| docs/前端目录结构设计.md | 前端工程结构 |
| docs/数据库表结构预览.txt | 表结构预览 |
| docs/智慧教学平台.openapi.json | Apifox 导入文件 |
| docs/MNT-SmartTeaching轻量化智能教学管理系统需求规格说明文档.docx | 需求规格说明书 |

## 注意事项

- Nacos、Nginx 为本地运行环境，不入库，克隆后需自行准备
- 单体版（smart-teaching-backend）为历史架构存档，仅保留本地
- 数据库当前为多服务共享单库过渡，按域分库演进已在架构中预留

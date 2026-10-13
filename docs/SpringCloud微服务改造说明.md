# MNT-SmartTeaching Spring Cloud 微服务改造说明

## 背景

原项目为单体 Spring Boot 应用（`mnt-st/smart-teaching-backend`，端口 8080），所有业务模块（用户、课程、选课、考勤、预警、AI、数据分析、教学）打包在一个进程内。本次在备份工程基础上，将其改造为 Spring Cloud 微服务架构，保持功能可用、数据库不变，并采用与原单体一致的分层与编码风格。

改造后的工程位于 `mnt-st/smart-teaching-cloud`，原单体工程保留未动，可随时回退对照。

## 做了什么

### 一、工程拆分（11 个 Maven 模块）

- `common`（smart-teaching-common）：公共库模块，承载 Result 统一返回、异常体系、常量、工具类、BaseEntity，并把 User / Course / CourseSchedule 三个共享实体迁入；新增 `InternalApiCheckFilter` 拦截 `/api/internal/**`，校验内部调用头。
- `api`（smart-teaching-api）：服务契约模块，定义 4 个 Feign 客户端（UserFeignClient / CourseFeignClient / WarningFeignClient / SelectionFeignClient）与 3 个跨服务 DTO（UserInfoVO / CourseInfoVO / TruantWarningDTO）；`InternalFeignInterceptor` 自动为 Feign 出站请求附加内部标识头。
- `gateway-service`（8080）：Spring Cloud Gateway（响应式 WebFlux），统一入口，按路径前缀负载均衡转发至各业务服务；内置 CORS 配置；路由前缀与原单体 Controller 前缀完全一致。
- 8 个业务服务：uaa(8081) / course(8082) / selection-score(8083) / attendance(8084) / warning(8085) / ai(8086) / analysis(8087) / teaching(8088)，各自包含独立启动类、Security/JWT/Redis/MyBatis-Plus/Swagger 配置与按域迁移的业务代码。

### 二、技术栈与版本（与原单体对齐）

- Spring Boot 4.0.8（原单体 4.1.0，因 Spring Cloud 2025.1.0 官方兼容矩阵要求 Boot 4.0.x，见"注意问题"）
- Spring Cloud 2025.1.0 + Spring Cloud Alibaba 2025.1.0.0（唯一适配 Boot 4.0.x 的官方组合）
- Nacos 3.2.4 注册中心（standalone，本地单机）
- MyBatis-Plus 3.5.17 / fastjson2 2.0.53 / hutool 5.8.30 / easyexcel 3.3.2 / jjwt 0.12.6 / springdoc 2.6.0（与原单体一致）
- JDK 17 编译（本机 JDK 23 运行 Maven）

### 三、跨服务调用 Feign 化（核心改造）

按"企业解耦"原则，服务间业务数据查询全部改为 OpenFeign 调用，禁止直连其他服务的数据库表：

- **ai-service**：学生身份校验由本地 `userMapper.selectById` 改为 `UserFeignClient.getUserById`。
- **analysis-service**：仪表盘教师角色校验改为 `UserFeignClient`（跨 8 表聚合的 SQL 保留在 analysis 本地 Mapper）。
- **selection-score-service**：课程校验、取 teacherId / 状态改为 `CourseFeignClient`。
- **teaching-service**：公告权限校验、问答课程校验与选课状态、评价课程列表/学生姓名脱敏/教师课程列表全部改为 `CourseFeignClient` / `UserFeignClient` / `SelectionFeignClient`。
- **attendance-service**：课程名/教师名、创建签到校验改为 Feign；**考勤生成旷课预警改为调 `WarningFeignClient.createTruancyWarning`**，预警写入逻辑移交 warning-service（幂等 upsert）。
- **course-service**：课表排课的学生校验改为 `UserFeignClient`。
- **warning-service**：新增 `createTruancyWarning` 接口（存在未处理旷课预警则更新、否则新建）。
- 各服务新增 4 个内部 Controller（`/api/internal/user`、`/api/internal/course`、`/api/internal/selection`、`/api/internal/warning`），统一由 `X-Internal-Call: smart-teaching-cloud` 头保护，外部访问返回 403。

安全链路（getCurrentUser / CustomUserDetailsService / 登录）各服务保留本地精简版 UserMapper，仅做 BaseMapper 查询，不跨服务。

### 四、数据库决策

**数据库完全未动**，8 个服务共享原 `smart_teaching` 单库（21 张表、大量 JOIN、Dashboard 跨 8 表统计）。这是"服务进程边界 + 共享数据库"的过渡方案：先以服务为边界解耦进程与代码，后续再按域逐步分库，避免一次性拆库破坏现有功能与报表。

### 五、冒烟验证结果（本机实测通过）

- Nacos 注册中心：gRPC 9848/9849 + API 8089 正常，9 个服务全部注册成功。
- 网关登录：`POST /api/auth/login`（admin/123456）返回 200 + token。
- 带 JWT 访问业务接口全部 200：用户列表、课程列表、课表、公告、问答、评价、作业、选课、成绩、预警、仪表盘（admin 视图）。
- Feign 链路实测通过：teaching→course/selection/user、attendance→course/user、selection-score→course、analysis→user 均正常返回。

## 弃置了什么

- **单体进程部署方式**：不再以单个 jar 承载全部业务，改为 9 个独立进程（1 网关 + 8 业务服务）。
- **跨域直连他人数据库表**：原单体中"查询别的模块表"的 Mapper 调用（如 teaching 直接查 course 表、attendance 直接查 warning 表）全部移除，改为 Feign 调用。
- **非 uaa 服务的完整版 UserMapper**：除 uaa 外的 7 个服务的 UserMapper 精简为仅 BaseMapper<User>，对应 UserMapper.xml（含 selectUserPage 等）删除；只保留安全链路所需的最小查询。
- **course-service 中 teaching 域的专属 SQL**（selectTeacherCoursesWithStat）：原样保留在 teaching-service 本地 Mapper，course-service 不再持有。
- **YAML 声明式网关路由**：Spring Cloud 2025.1.0 + Boot 4.0.8 下 YAML `spring.cloud.gateway.routes` 未按预期生效，改为 **Java DSL 路由配置**（GatewayRouteConfig），行为一致且可编译期校验。
- **Nacos 内置控制台默认端口 8080**：与网关冲突，改为独立端口 18081；Nacos 以 server 部署模式启动（不加载控制台 context）。

## 需要注意的问题

- **版本兼容矩阵**：Spring Cloud 2025.1.0 官方只兼容 Spring Boot 4.0.x，不能沿用单体的 4.1.0，否则启动报"Spring Boot not compatible with this Spring Cloud release train"。已统一降到 4.0.8。
- **Gateway 依赖改名**：Spring Cloud 2025.x 起网关 starter 为 `spring-cloud-starter-gateway-server-webflux`，旧名 `spring-cloud-starter-gateway` 在 BOM 中不存在；网关模块严禁引入 `spring-boot-starter-web`（会与 WebFlux 冲突）。
- **Nacos 3.2.4 启动的三个坑**：首次启动会交互式要求输入 token secret key（startup.cmd 检测到配置值为空即 `set /p` 卡住），需在 `conf/application.properties` 填好 `nacos.core.auth.plugin.nacos.token.secret.key`（Base64、≥32 字节）与 identity key/value；Derby 驱动在 `plugins` 目录，必须通过 `-Dloader.path` 加载（Nacos 3.x 已从 fat jar 中移出）；控制台与 server 端口需用 `nacos.console.port` 单独指定，避免与网关 8080 冲突。
- **共享数据库的边界纪律**：当前阶段仍是单库，跨服务只允许经 Feign 取数，不允许新增直连其他服务表的 Mapper；这是向分库演进前的纪律约束。
- **JWT 一致性**：所有服务必须使用同一密钥与 Redis 令牌版本号机制，否则网关转发后下游无法解析 token；本次已全服务统一。
- **内部接口安全**：`/api/internal/**` 已在各服务 SecurityConfig 放行，由 InternalApiCheckFilter 校验内部调用头，禁止外部直接调用。
- **uploads 目录**：各服务静态文件映射统一指向 `smart-teaching-cloud/uploads`，网关 `/files/**` 转发到 uaa-service 统一提供。
- **单独构建子模块**：Windows 下若目标 jar 被运行中的进程占用，Maven repackage 会失败，先停止对应服务再打包。

## 为什么这样做

- **企业级解耦**：单体中任何模块的发布都要连带整个应用重启，改考勤一行代码也会牵连 AI、分析等服务；按域拆分为独立进程后，模块可独立开发、独立部署、独立扩容。
- **职责边界清晰**：服务间通过 Feign 契约调用，不再共享 Mapper 与表访问，接口即边界，便于多团队并行开发与代码审查。
- **为演进铺路**：先做进程与契约解耦、共享库过渡，未来拆库时只需替换 Feign 客户端的数据源，业务代码零改动。
- **基础设施就位**：网关统一入口、统一跨域、统一鉴权前置，后续可平滑加入限流、熔断、灰度、链路追踪等企业能力。

## 这样做的好处

- 故障隔离：单个服务崩溃不影响其他服务（如 AI 服务异常不会拖垮选课）。
- 独立演进：课程域升级 Spring 版本或调整表结构，不影响考勤/预警域。
- 弹性伸缩：考勤高峰期可只对 attendance-service 水平扩容。
- 团队协作：按域分工，契约先行，前后端联调接口不变。
- 可观测性基础：为后续接入 SkyWalking/Prometheus 等监控提供标准化的服务注册与调用链基础。

## 启动方式

一键启动脚本：`mnt-st/smart-teaching-cloud/start-all.cmd`（自动拉起 Nacos 并启动 9 个服务）。

前提：MySQL(3306) 与 Redis(6379) 已在运行，JDK 位于 `C:\Program Files\Java\jdk-23`。

访问入口：网关 `http://localhost:8080`；Nacos 控制台 `http://localhost:18081/nacos`（nacos/nacos）。

## 服务端口一览

| 服务 | 端口 | 职责 |
| --- | --- | --- |
| gateway-service | 8080 | API 网关（统一入口） |
| uaa-service | 8081 | 用户/认证/组织 |
| course-service | 8082 | 课程/课表 |
| selection-score-service | 8083 | 选课/成绩 |
| attendance-service | 8084 | 考勤 |
| warning-service | 8085 | 学业预警 |
| ai-service | 8086 | AI 助教 |
| analysis-service | 8087 | 数据分析/仪表盘 |
| teaching-service | 8088 | 公告/问答/评价/作业 |
| Nacos | 8848/9848/9849/8089 | 注册中心（console 18081） |

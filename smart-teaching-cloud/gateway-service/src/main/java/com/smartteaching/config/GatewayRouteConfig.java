package com.smartteaching.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置（Java DSL 方式）
 * <p>
 * 使用 Java DSL 而非 YAML 声明路由，便于 IDE 校验与调试；
 * 路由前缀与原单体 Controller 前缀完全一致，前端 / Nginx 无需改动。
 * 每个路由按路径前缀负载均衡转发至对应业务服务。
 */
@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===== 用户域（登录/注册/用户管理/组织） =====
                .route("uaa-service", r -> r.path("/api/auth/**", "/api/user/**", "/api/org/**")
                        .uri("lb://uaa-service"))
                // ===== 课程域（课程/课程表） =====
                .route("course-service", r -> r.path("/api/course/**", "/api/schedule/**")
                        .uri("lb://course-service"))
                // ===== 选课/成绩域 =====
                .route("selection-score-service", r -> r.path("/api/selection/**", "/api/score/**")
                        .uri("lb://selection-score-service"))
                // ===== 考勤域 =====
                .route("attendance-service", r -> r.path("/api/attendance/**")
                        .uri("lb://attendance-service"))
                // ===== 学业预警域 =====
                .route("warning-service", r -> r.path("/api/warning/**")
                        .uri("lb://warning-service"))
                // ===== AI 助教域 =====
                .route("ai-service", r -> r.path("/api/ai/**", "/api/ollama/**")
                        .uri("lb://ai-service"))
                // ===== 数据分析/仪表盘域 =====
                .route("analysis-service", r -> r.path("/api/dashboard/**")
                        .uri("lb://analysis-service"))
                // ===== 教学服务域（公告/问答/评价/作业） =====
                .route("teaching-service", r -> r.path("/api/notice/**", "/api/qa/**", "/api/evaluation/**", "/api/homework/**")
                        .uri("lb://teaching-service"))
                // ===== 静态文件（各服务共享同一 uploads 目录，统一由 uaa-service 提供静态映射） =====
                .route("files-static", r -> r.path("/files/**")
                        .uri("lb://uaa-service"))
                .build();
    }
}

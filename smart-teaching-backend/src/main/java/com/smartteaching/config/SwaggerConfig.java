package com.smartteaching.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger (SpringDoc OpenAPI) 配置类
 * <p>
 * 功能：
 * 1. API 文档基本信息配置（标题、描述、版本、联系方式）
 * 2. JWT Token 认证配置（支持在 Swagger UI 中携带 Token 测试接口）
 * 3. API 分组配置（按模块分组，便于管理）
 * <p>
 * 访问地址：
 * - Swagger UI: http://localhost:8080/api/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8080/api/v3/api-docs
 *
 * @author SmartTeaching
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 核心配置
     * <p>
     * 配置内容：
     * - API 文档标题、描述、版本
     * - 联系人信息
     * - 许可证信息
     * - JWT 安全认证方案
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 1. 配置文档基本信息
                .info(new Info()
                        .title("智慧教学平台 API 文档")
                        .description("智慧教学平台后端接口文档，包含用户管理、课程管理、教学资源等模块")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartTeaching Team")
                                .email("support@smartteaching.com")
                                .url("https://www.smartteaching.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // 2. 配置 JWT 安全认证
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入 JWT Token，格式：Bearer {token}")))
                // 3. 全局应用安全认证（所有接口都需要携带 Token）
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    /**
     * API 分组配置：公开接口分组
     * <p>
     * 包含：登录、注册、验证码等不需要认证的接口
     *
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("公开接口")
                .displayName("公开接口")
                .pathsToMatch("/auth/**", "/public/**")
                .build();
    }

    /**
     * API 分组配置：用户管理接口
     *
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .displayName("用户管理")
                .pathsToMatch("/api/users/**")
                .build();
    }

    /**
     * API 分组配置：课程管理接口
     *
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi courseApi() {
        return GroupedOpenApi.builder()
                .group("课程管理")
                .displayName("课程管理")
                .pathsToMatch("/api/courses/**")
                .build();
    }

    /**
     * API 分组配置：所有接口（默认分组）
     *
     * @return GroupedOpenApi
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("全部接口")
                .displayName("全部接口")
                .pathsToMatch("/**")
                .packagesToScan("com.smartteaching.controller")
                .build();
    }
}
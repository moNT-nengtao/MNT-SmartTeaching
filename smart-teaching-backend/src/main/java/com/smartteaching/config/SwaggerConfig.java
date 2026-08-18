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
 * Swagger接口文档配置(SpringDoc OpenAPI)
 * 配置文档信息、JWT令牌、接口分组
 * 访问：/api/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI核心配置
     * 设置文档标题版本，配置JWT授权，swagger页面可以填token测试接口
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
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
                // 配置Bearer JWT认证
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("格式：Bearer {token}")))
                // 默认所有接口都带上token认证
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    /**
     * 公开接口分组：登录注册验证码，不需要token
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
     * 用户管理分组
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
     * 课程管理分组
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
     * 全部接口分组，扫描controller包所有接口
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

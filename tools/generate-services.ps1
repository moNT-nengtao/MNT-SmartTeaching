$ErrorActionPreference = "Stop"
$root = "C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud"
$uploadAbs = "C:/Users/猫猫君/Desktop/MNT-SmartTeaching/mnt-st/smart-teaching-cloud/uploads"

# 服务清单：名称 | 注册名 | 端口 | 启动类 | 需要 webflux
$services = @(
    @{ dir = "uaa-service";        app = "uaa-service";              port = 8081; main = "UaaApplication";        webflux = $false },
    @{ dir = "course-service";     app = "course-service";           port = 8082; main = "CourseApplication";     webflux = $false },
    @{ dir = "selection-score-service"; app = "selection-score-service"; port = 8083; main = "SelectionApplication"; webflux = $false },
    @{ dir = "attendance-service"; app = "attendance-service";       port = 8084; main = "AttendanceApplication";  webflux = $false },
    @{ dir = "warning-service";    app = "warning-service";          port = 8085; main = "WarningApplication";     webflux = $false },
    @{ dir = "ai-service";         app = "ai-service";               port = 8086; main = "AiApplication";          webflux = $true  },
    @{ dir = "analysis-service";   app = "analysis-service";         port = 8087; main = "AnalysisApplication";    webflux = $false },
    @{ dir = "teaching-service";   app = "teaching-service";         port = 8088; main = "TeachingApplication";    webflux = $false }
)

$pomTemplate = @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.smartteaching</groupId>
        <artifactId>smart-teaching-cloud</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>{SERVICE_DIR}</artifactId>
    <name>{SERVICE_DIR}</name>
    <description>{SERVICE_NAME} 微服务（Spring Cloud Alibaba + Nacos + OpenFeign）</description>

    <dependencies>
        <!-- 内部模块 -->
        <dependency>
            <groupId>com.smartteaching</groupId>
            <artifactId>smart-teaching-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.smartteaching</groupId>
            <artifactId>smart-teaching-api</artifactId>
        </dependency>

        <!-- Nacos 注册发现 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
{WEBFLUX_DEPS}
        <!-- spring-security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-jsqlparser</artifactId>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
        </dependency>

        <!-- 参数校验 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- 工具库（与原单体保持一致） -->
        <dependency>
            <groupId>com.alibaba.fastjson2</groupId>
            <artifactId>fastjson2</artifactId>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
        </dependency>

        <!-- Swagger -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
'@

$mainTemplate = @'
package com.smartteaching;

import com.smartteaching.common.utils.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {SERVICE_NAME} 启动类
 * 扫描 com.smartteaching（common/api/本服务全部类）
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtUtil.class)
@EnableFeignClients(basePackages = "com.smartteaching.api")
@EnableScheduling
public class {MAIN} {

    public static void main(String[] args) {
        SpringApplication.run({MAIN}.class, args);
    }
}
'@

$securityConfigTemplate = @'
package com.smartteaching.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * SpringSecurity 安全配置类（微服务版）
 * 处理鉴权、会话、跨域、过滤器注册等全局安全规则
 * 相比单体新增：放行 /api/internal/** 内部接口（由 InternalApiCheckFilter 校验标识）
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .requestMatchers("/api/auth/login","/api/auth/logout","/api/register","/api/captcha").permitAll()
                        // 服务间内部接口：允许 Feign 调用（InternalApiCheckFilter 校验内部标识）
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        http.addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class);

        return http.build();
    }

}
'@

$jwtFilterTemplate = @'
package com.smartteaching.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT过滤器（微服务版，与原单体逻辑一致）
 * 每个服务保留独立鉴权，服务间 Feign 调用走 /api/internal/** 放行路径
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("进入JWT过滤器，请求路径:{}，请求方式:{}", request.getRequestURI(), request.getMethod());

        String headerName = jwtUtil.getHeader();
        String tokenPrefix = jwtUtil.getTokenPrefix();
        String tokenHeader = request.getHeader(headerName);

        String token = null;
        String username = null;

        if (tokenHeader != null && tokenHeader.startsWith(tokenPrefix)) {
            token = tokenHeader.substring(tokenPrefix.length());

            String blackKey = "jwt:blacklist:" + token;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blackKey))) {
                log.warn("token在黑名单，已登出拒绝访问");
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Result<?> result = Result.error("已登出，请重新登录");
                new ObjectMapper().writeValue(response.getWriter(), result);
                return;
            }

            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.getUsernameByToken(token);

                Long jwtUserId;
                Long jwtVersion;
                try {
                    jwtUserId = jwtUtil.getUserIdByToken(token);
                    jwtVersion = jwtUtil.getTokenVersionByToken(token);
                } catch (Exception e) {
                    log.warn("token载荷缺失userId/tokenVersion，拒绝访问");
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Result<?> result = Result.error("令牌非法，请重新登录");
                    new ObjectMapper().writeValue(response.getWriter(), result);
                    return;
                }

                String redisKey = "user:token:version:" + jwtUserId;
                String realVersionStr = stringRedisTemplate.opsForValue().get(redisKey);

                if (realVersionStr == null || !Long.valueOf(realVersionStr).equals(jwtVersion)) {
                    log.warn("用户{}令牌版本不匹配，密码已修改，强制下线", jwtUserId);
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Result<?> result = Result.error("密码已修改，请重新登录");
                    new ObjectMapper().writeValue(response.getWriter(), result);
                    return;
                }

                log.debug("Token 验证成功，用户名: {}", username);
            } else {
                log.warn("Token 验证失败，可能已过期或签名错误");
            }
        } else {
            log.debug("请求头中未包含有效的 Authorization 信息");
        }

        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.info("用户 [{}] 通过 JWT Token 认证成功", username);
        }

        filterChain.doFilter(request, response);
    }
}
'@

$redisConfigTemplate = @'
package com.smartteaching.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置（微服务版，与原单体一致）
 * 解决redis乱码，自定义json序列化，提供两套模板
 */
@Configuration
public class RedisConfig {

    public static class CustomJacksonRedisSerializer implements RedisSerializer<Object> {

        private final ObjectMapper objectMapper;

        public CustomJacksonRedisSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(Object value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return objectMapper.writeValueAsBytes(value);
            } catch (JsonProcessingException e) {
                throw new SerializationException("Redis序列化对象失败", e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(bytes, Object.class);
            } catch (Exception e) {
                throw new SerializationException("Redis反序列化对象失败", e);
            }
        }
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        CustomJacksonRedisSerializer jacksonSerializer = new CustomJacksonRedisSerializer(redisObjectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
'@

$mybatisConfigTemplate = @'
package com.smartteaching.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * MyBatis‑Plus配置（微服务版）
 * 每个服务仅扫描本模块拷贝的 mapper 接口
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.smartteaching.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(100L);
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);

        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new DefaultMetaObjectHandler());
        return globalConfig;
    }

    public static class DefaultMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

    }
}
'@

$webMvcConfigTemplate = @'
package com.smartteaching.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebMvc配置（微服务版）
 * 上传目录统一指向 smart-teaching-cloud/uploads（绝对路径），保证 /files/** 全局可访问
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.access-path:/files/**}")
    private String accessPath;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private Boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private Long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(accessPath)
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600)
                .resourceChain(true);

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        objectMapper.registerModule(javaTimeModule);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
'@

$swaggerConfigTemplate = @'
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
 * Swagger接口文档配置（微服务版）
 * 各服务独立文档，访问：/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("{SERVICE_NAME} API 文档")
                        .description("智慧教学平台微服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SmartTeaching Team")
                                .email("support@smartteaching.com")
                                .url("https://www.smartteaching.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("格式：Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

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
'@

$ymlTemplate = @'
# ============================================================
#  {SERVICE_NAME} 微服务配置
# ============================================================

spring:
  application:
    name: {SERVICE_APP}
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        # 本地单机 Nacos 默认命名空间即可，无需修改
  # ---------- 数据源【本地MySQL，与原单体共用 smart_teaching 库】 ----------
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/smart_teaching?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 600000
      connection-timeout: 30000
      pool-name: SmartTeachingHikariPool

  # ---------- Redis【本地】 ----------
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: 123456
      timeout: 10000ms
      database: 0
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: -1ms

  servlet:
    multipart:
      enabled: true
      max-file-size: 20MB
      max-request-size: 200MB
      file-size-threshold: 2MB

server:
  port: {SERVICE_PORT}

# ---------- JWT（所有服务共享同一密钥与规则，保证跨服务校验一致） ----------
jwt:
  secret: abcdefghijklmnopqrstuvwxyz1234567890
  expiration: 604800000
  header: Authorization
  token-prefix: "Bearer "

# 上传目录统一为微服务工程根下的 uploads（绝对路径），前端 /files/** 全局可访问
file:
  upload-path: {UPLOAD_ABS}
  avatar:
    relative-path: uploads/avatars

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true

logging:
  level:
    root: INFO
    com.smartteaching: DEBUG
    com.smartteaching.**.mapper: DEBUG
    org.springframework.security: DEBUG
'@

foreach ($s in $services) {
    $dir = "$root\$($s.dir)"
    $javaPkgDir = "$dir\src\main\java\com\smartteaching"
    New-Item -ItemType Directory -Path $javaPkgDir -Force | Out-Null
    $webfluxDeps = ""
    if ($s.webflux) {
        $webfluxDeps = "`n        <!-- WebFlux（Ollama 响应式调用） -->`n        <dependency>`n            <groupId>org.springframework.boot</groupId>`n            <artifactId>spring-boot-starter-webflux</artifactId>`n        </dependency>"
    }
    $pom = $pomTemplate.Replace("{SERVICE_DIR}", $s.dir).Replace("{SERVICE_NAME}", $s.app)
    $pom = $pom.Replace("{WEBFLUX_DEPS}", $webfluxDeps)
    Set-Content -Path "$dir\pom.xml" -Value $pom -Encoding UTF8

    $main = $mainTemplate.Replace("{SERVICE_NAME}", $s.app).Replace("{MAIN}", $s.main)
    Set-Content -Path "$dir\src\main\java\com\smartteaching\$($s.main).java" -Value $main -Encoding UTF8

    $configDir = "$dir\src\main\java\com\smartteaching\config"
    New-Item -ItemType Directory -Path $configDir -Force | Out-Null
    Set-Content -Path "$configDir\SecurityConfig.java" -Value $securityConfigTemplate -Encoding UTF8
    Set-Content -Path "$configDir\JwtAuthenticationFilter.java" -Value $jwtFilterTemplate -Encoding UTF8
    Set-Content -Path "$configDir\RedisConfig.java" -Value $redisConfigTemplate -Encoding UTF8
    Set-Content -Path "$configDir\MybatisPlusConfig.java" -Value $mybatisConfigTemplate -Encoding UTF8
    Set-Content -Path "$configDir\WebMvcConfig.java" -Value $webMvcConfigTemplate -Encoding UTF8
    Set-Content -Path "$configDir\SwaggerConfig.java" -Value $swaggerConfigTemplate.Replace("{SERVICE_NAME}", $s.app) -Encoding UTF8

    $yml = $ymlTemplate.Replace("{SERVICE_NAME}", $s.app).Replace("{SERVICE_APP}", $s.app).Replace("{SERVICE_PORT}", [string]$s.port).Replace("{UPLOAD_ABS}", $uploadAbs)
    Set-Content -Path "$dir\src\main\resources\application.yml" -Value $yml -Encoding UTF8

    Write-Host "generated: $($s.dir) (port $($s.port))"
}
Write-Host "DONE"

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
 * SpringSecurity 安全配置类
 * 处理鉴权、会话、跨域、过滤器注册等全局安全规则
 */
@Configuration
@EnableWebSecurity // 开启SpringSecurity Web安全支持
public class SecurityConfig {

    // 注入JWT过滤器
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码加密器 Bean
     * 仅容器占位，登录密码校验由自定义AuthService手写MD5实现
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * 跨域配置Bean，解决前端浏览器OPTIONS预检拦截
     */
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

    /**
     * 核心安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 关闭csrf（前后端分离JWT无需）
                .csrf(csrf -> csrf.disable())
                // 启用跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 无状态Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限匹配规则
                .authorizeHttpRequests(auth -> auth
                        // 放行所有OPTIONS预检请求
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        // 放行登录接口，路径与Controller完全匹配
                        .requestMatchers("/api/auth/login","/api/auth/logout","/api/register","/api/captcha").permitAll()
                        // 管理员接口权限
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 其余接口必须登录
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // 将JWT过滤器挂载在鉴权过滤器之前，修复旧锚点失效问题
        http.addFilterBefore(jwtAuthenticationFilter, AuthorizationFilter.class);

        return http.build();
    }

}

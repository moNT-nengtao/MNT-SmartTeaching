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
 * JWT 认证过滤器
 * <p>
 * 职责：拦截每个请求，从请求头中提取 JWT Token，验证合法性，
 * 并将用户信息注入 Spring Security 上下文，实现自动登录。
 * <p>
 * 执行顺序：在 Spring Security 过滤器链中，先于 AuthorizationFilter
 *
 * @author SmartTeaching
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // @Lazy 延迟注入，解决Bean循环依赖启动报错
    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    //新增Redis黑名单
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每个请求执行一次的核心过滤方法
     *
     * @param request      HTTP 请求对象
     * @param response     HTTP 响应对象
     * @param filterChain  过滤器链，用于将请求传递给下一个过滤器
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 新增：跨域OPTIONS预检请求直接放行，不执行Token校验逻辑
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 打印日志，用于调试查看请求是否进入JWT过滤器
        log.info("进入JWT过滤器，请求路径:{}，请求方式:{}", request.getRequestURI(), request.getMethod());

        // 1. 从请求头中获取 Authorization 字段
        String headerName = jwtUtil.getHeader();          // 默认: Authorization
        String tokenPrefix = jwtUtil.getTokenPrefix();    // 默认: Bearer （末尾已含空格）
        String tokenHeader = request.getHeader(headerName);

        String token = null;
        String username = null;

        // 2. 检查请求头是否存在且以正确前缀开头
        // 注意：tokenPrefix 已经包含空格（如 "Bearer "），直接使用即可
        if (tokenHeader != null && tokenHeader.startsWith(tokenPrefix)) {
            // 截取真正的 Token（去掉前缀部分）
            token = tokenHeader.substring(tokenPrefix.length());

            //====================【新增黑名单校验】====================
            String blackKey = "jwt:blacklist:" + token;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blackKey))) {
                log.warn("token在黑名单，已登出拒绝访问");
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Result<?> result = Result.error( "已登出，请重新登录");
                new ObjectMapper().writeValue(response.getWriter(), result);
                return;
            }
            //========================================================

            // 3. 验证 Token 是否合法（签名正确且未过期）
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.getUsernameByToken(token);

                //====================【新增令牌版本号校验：修改密码强制全部设备下线】====================
                Long jwtUserId;
                Long jwtVersion;
                try {
                    // 从JWT载荷解析用户ID与令牌版本号
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
                // 查询Redis保存的真实令牌版本
                String redisKey = "user:token:version:" + jwtUserId;
                String realVersionStr = stringRedisTemplate.opsForValue().get(redisKey);
                // Redis无版本记录 或者 JWT携带版本与Redis真实版本不一致，判定令牌失效
                if (realVersionStr == null || !Long.valueOf(realVersionStr).equals(jwtVersion)) {
                    log.warn("用户{}令牌版本不匹配，密码已修改，强制下线", jwtUserId);
                    response.setContentType("application/json;charset=utf-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    Result<?> result = Result.error("密码已修改，请重新登录");
                    new ObjectMapper().writeValue(response.getWriter(), result);
                    return;
                }
                //====================================================================================

                log.debug("Token 验证成功，用户名: {}", username);
            } else {
                log.warn("Token 验证失败，可能已过期或签名错误");
            }
        } else {
            // 没有携带 Token 或前缀不正确，不处理（放行后由 SecurityConfig 决定是否拦截）
            log.debug("请求头中未包含有效的 Authorization 信息");
        }

        // 4. 如果 Token 合法，直接写入上下文，移除 `getAuthentication() == null` 判断
        // 原因：AnonymousAuthenticationFilter会提前放入匿名认证对象，导致原条件永远不触发
        if (username != null) {
            // 5. 从数据库加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 6. 创建 Spring Security 认证令牌
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 7. 设置请求详情（IP 地址、Session ID 等）
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 8. 将认证信息存入 SecurityContext，表示当前用户已登录
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.info("用户 [{}] 通过 JWT Token 认证成功", username);
        }

        // 9. 继续执行后续过滤器链
        filterChain.doFilter(request, response);
    }
}

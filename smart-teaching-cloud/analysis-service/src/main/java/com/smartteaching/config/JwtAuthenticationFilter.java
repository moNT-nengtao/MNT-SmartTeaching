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

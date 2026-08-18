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
 * JWT过滤器，每个请求进来都会执行一次
 * 作用：解析token、校验token，把登录用户放到Security上下文
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

        // OPTIONS跨域预检请求直接放行，不校验token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("进入JWT过滤器，请求路径:{}，请求方式:{}", request.getRequestURI(), request.getMethod());

        // 获取请求头配置
        String headerName = jwtUtil.getHeader();
        String tokenPrefix = jwtUtil.getTokenPrefix();
        String tokenHeader = request.getHeader(headerName);

        String token = null;
        String username = null;

        // 判断请求头是否携带token，并且前缀正确
        if (tokenHeader != null && tokenHeader.startsWith(tokenPrefix)) {
            // 去掉Bearer前缀，拿到真正token
            token = tokenHeader.substring(tokenPrefix.length());

            // 判断token是否在redis黑名单（用户已经登出）
            String blackKey = "jwt:blacklist:" + token;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blackKey))) {
                log.warn("token在黑名单，已登出拒绝访问");
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Result<?> result = Result.error("已登出，请重新登录");
                new ObjectMapper().writeValue(response.getWriter(), result);
                return;
            }

            // 校验token签名、是否过期
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.getUsernameByToken(token);

                // 从token取出userId和令牌版本号
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

                // 获取redis中保存的用户真实令牌版本
                String redisKey = "user:token:version:" + jwtUserId;
                String realVersionStr = stringRedisTemplate.opsForValue().get(redisKey);

                // 版本不一致，代表修改过密码，强制下线
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
            // 请求头没有token，不做处理，交给security后续判断
            log.debug("请求头中未包含有效的 Authorization 信息");
        }

        // token解析成功，把用户信息存入Security上下文
        if (username != null) {
            // 查询数据库拿到用户完整信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 构造security认证对象
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 保存请求信息(ip等)
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 存入上下文，标记当前用户已登录
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            log.info("用户 [{}] 通过 JWT Token 认证成功", username);
        }

        // 放行，走后面的过滤器
        filterChain.doFilter(request, response);
    }
}

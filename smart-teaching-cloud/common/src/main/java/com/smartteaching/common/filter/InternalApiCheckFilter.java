package com.smartteaching.common.filter;

import com.smartteaching.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 内部接口防护过滤器
 * 仅放行携带 X-Internal-Call 标识的服务间 Feign 请求，
 * 防止外部绕过网关直接访问 /api/internal/** 内部接口
 */
@Slf4j
@Component
public class InternalApiCheckFilter extends OncePerRequestFilter {

    public static final String INTERNAL_HEADER = "X-Internal-Call";
    public static final String INTERNAL_HEADER_VALUE = "smart-teaching-cloud";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 非内部接口直接放行
        if (uri == null || !uri.startsWith("/api/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 内部接口：校验内部调用标识
        String header = request.getHeader(INTERNAL_HEADER);
        if (INTERNAL_HEADER_VALUE.equals(header)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("内部接口被外部访问拒绝：{}", uri);
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"code\":0,\"msg\":\"禁止访问内部接口\"}");
    }
}

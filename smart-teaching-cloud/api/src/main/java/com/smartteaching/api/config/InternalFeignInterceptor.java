package com.smartteaching.api.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * 服务间内部调用标识
 * 所有 Feign 请求自动携带 X-Internal-Call 头，
 * 服务端 InternalApiCheckFilter 仅放行携带该标识的 /api/internal/** 请求，
 * 防止外部绕过网关直接访问内部接口
 */
@Component
public class InternalFeignInterceptor implements RequestInterceptor {

    /** 内部调用标识头 */
    public static final String INTERNAL_HEADER = "X-Internal-Call";
    public static final String INTERNAL_HEADER_VALUE = "smart-teaching-cloud";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(INTERNAL_HEADER, INTERNAL_HEADER_VALUE);
    }
}

package com.smartteaching;

import com.smartteaching.common.utils.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * teaching-service 启动类
 * 扫描 com.smartteaching（common/api/本服务全部类）
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtUtil.class)
@EnableFeignClients(basePackages = "com.smartteaching.api")
@EnableScheduling
public class TeachingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeachingApplication.class, args);
    }
}

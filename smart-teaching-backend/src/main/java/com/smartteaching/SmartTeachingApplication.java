package com.smartteaching;

import com.smartteaching.common.utils.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(JwtUtil.class)
@EnableScheduling
public class SmartTeachingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTeachingApplication.class, args);
    }


}

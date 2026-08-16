package com.smartteaching;

import com.smartteaching.common.utils.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtUtil.class)
public class SmartTeachingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTeachingApplication.class, args);
    }


}

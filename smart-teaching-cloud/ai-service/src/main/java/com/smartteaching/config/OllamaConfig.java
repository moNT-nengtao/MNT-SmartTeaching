package com.smartteaching.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName OllamaConfig
 * @Description
 * @Author MNT
 * @Date 2026/9/2 22:04
 **/
@Data
@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {
    private String host = "http://localhost:11434";
    private String model = "qwen3:8b";
    private Integer timeout = 60;
    private Integer maxHistory = 10;
    /** 每日AI问答次数上限（按用户统计） */
    private Integer dailyLimit = 30;

    /**
     * 获取可用的 Ollama 地址。
     * <p>注意：Ollama 安装时会设置系统环境变量 OLLAMA_HOST（如 0.0.0.0:11434），
     * Spring Boot 宽松绑定会将其注入到本配置的 host 字段，导致缺少协议前缀。
     * 这里统一做规范化：补全 http 协议头、0.0.0.0 监听地址转为本机回环地址。</p>
     */
    public String getResolvedHost() {
        String h = this.host;
        if (h == null || h.isBlank()) {
            return "http://localhost:11434";
        }
        h = h.trim();
        // 去掉尾部斜杠，避免拼接 /api/chat 时出现双斜杠
        while (h.endsWith("/")) {
            h = h.substring(0, h.length() - 1);
        }
        // 0.0.0.0 是监听地址，客户端连接应使用本机回环地址
        if (h.startsWith("0.0.0.0:")) {
            h = "127.0.0.1" + h.substring(h.indexOf(':'));
        }
        // 补充协议前缀
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://" + h;
        }
        return h;
    }
}
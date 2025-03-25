package com.dbmanage.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekProperties {
    private String apiKey;
    private String baseUrl;
    private String chatUrl;
    private String model;
    private int timeoutSeconds;
} 
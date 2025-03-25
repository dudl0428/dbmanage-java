package com.dbmanage.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * AI模型配置属性类
 * 用于加载application.yml中定义的AI相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    
    private Map<String, String> systemPrompts;
} 
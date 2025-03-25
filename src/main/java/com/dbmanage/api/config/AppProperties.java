package com.dbmanage.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 应用程序配置属性类
 * 用于加载application.yml中定义的配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt;
    private Cors cors;
    private ErrorMessages errorMessages;

    /**
     * JWT配置
     */
    @Data
    public static class Jwt {
        private String secret;
        private long expiration;
    }

    /**
     * 跨域配置
     */
    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private long maxAge;
    }

    /**
     * 错误消息配置
     */
    @Data
    public static class ErrorMessages {
        private DeepseekError deepseek;
        private ConnectionError connection;
        private ValidationError validation;

        @Data
        public static class DeepseekError {
            private String extractContentFailed;
        }

        @Data
        public static class ConnectionError {
            private String nameExists;
            private String groupNotFound;
            private String groupNameExists;
            private String connectionNotInGroup;
        }

        @Data
        public static class ValidationError {
            private String fieldEmpty;
            private String fieldNameEmpty;
            private String fieldNameFormat;
            private String fieldTypeEmpty;
            private String fieldTypeNotSupported;
            private String fieldLengthPositive;
            private String fieldLengthMax;
            private String fieldDecimalNegative;
            private String fieldDecimalMax;
            private String fieldDecimalLength;
            private String fieldAutoIncrement;
            private String requestEmpty;
            private String tableNameEmpty;
            private String tableNameFormat;
            private String fieldsMin;
            private String fieldDuplicate;
            private String indexNameEmpty;
            private String indexFieldsMin;
            private String indexFieldNotExist;
            private String foreignKeyNameEmpty;
            private String foreignKeySrcFieldsMin;
            private String foreignKeyRefTableEmpty;
            private String foreignKeyRefFieldsMin;
            private String foreignKeyFieldNotExist;
            private String foreignKeyFieldCount;
        }
    }
} 
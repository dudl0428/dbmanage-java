package com.dbmanage.api.util;

import java.text.MessageFormat;

/**
 * 消息解析工具类
 * 用于处理带占位符的错误消息
 */
public class MessageResolver {

    /**
     * 格式化消息，替换占位符
     * 
     * @param template 消息模板，使用{0}, {1}等占位符
     * @param args 替换参数
     * @return 格式化后的消息
     */
    public static String format(String template, Object... args) {
        if (template == null) {
            return "";
        }
        
        return MessageFormat.format(template, args);
    }
} 
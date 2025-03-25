package com.dbmanage.api.exception;

/**
 * 数据库连接异常
 * 当数据库连接失败时抛出
 */
public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
} 
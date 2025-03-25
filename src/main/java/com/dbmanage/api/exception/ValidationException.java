package com.dbmanage.api.exception;

/**
 * 验证异常
 * 当数据验证失败时抛出此异常
 */
public class ValidationException extends RuntimeException {
    private String field;
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
} 
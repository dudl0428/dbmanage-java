package com.dbmanage.api.common;

import java.io.Serializable;

/**
 * 统一API响应结果封装类
 * 
 * @param <T> 数据类型
 */
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 字段
     */
    private String field;
    
    /**
     * 构造函数
     */
    private ApiResponse() {
    }
    
    /**
     * 构造函数
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param success 是否成功
     */
    private ApiResponse(Integer code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }
    
    /**
     * 成功响应
     * 
     * @param <T> 数据类型
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null, true);
    }
    
    /**
     * 成功响应
     * 
     * @param <T> 数据类型
     * @param data 数据
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, true);
    }
    
    /**
     * 成功响应
     * 
     * @param <T> 数据类型
     * @param message 消息
     * @param data 数据
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, true);
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(500, "操作失败", null, false);
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @param message 消息
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null, false);
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @param message 消息
     * @param field 字段名
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> error(String message, String field) {
        ApiResponse<T> response = new ApiResponse<>(400, message, null, false);
        response.setField(field);
        return response;
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @param code 状态码
     * @param message 消息
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, false);
    }
    
    /**
     * 失败响应
     * 
     * @param <T> 数据类型
     * @param code 状态码
     * @param message 消息
     * @param field 字段名
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message, String field) {
        ApiResponse<T> response = new ApiResponse<>(code, message, null, false);
        response.setField(field);
        return response;
    }
    
    /**
     * 自定义响应
     * 
     * @param <T> 数据类型
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param success 是否成功
     * @return ApiResponse 对象
     */
    public static <T> ApiResponse<T> custom(Integer code, String message, T data, boolean success) {
        return new ApiResponse<>(code, message, data, success);
    }
    
    // Getters and Setters
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
} 
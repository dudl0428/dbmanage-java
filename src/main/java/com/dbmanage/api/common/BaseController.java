package com.dbmanage.api.common;

import com.dbmanage.api.dto.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 基础控制器，提供通用功能
 */
public class BaseController {
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
    
    /**
     * 返回成功响应
     * @param <T> 数据类型
     * @return ResponseEntity对象
     */
    protected <T> ResponseEntity<ApiResponse<T>> success() {
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 返回带数据的成功响应
     * @param <T> 数据类型
     * @param data 数据
     * @return ResponseEntity对象
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * 返回带消息和数据的成功响应
     * @param <T> 数据类型
     * @param message 消息
     * @param data 数据
     * @return ResponseEntity对象
     */
    protected <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }
    
    /**
     * 返回失败响应
     * @param <T> 数据类型
     * @return ResponseEntity对象
     */
    protected <T> ResponseEntity<ApiResponse<T>> error() {
        return ResponseEntity.ok(ApiResponse.error());
    }
    
    /**
     * 返回带消息的失败响应
     * @param <T> 数据类型
     * @param message 消息
     * @return ResponseEntity对象
     */
    protected <T> ResponseEntity<ApiResponse<T>> error(String message) {
        return ResponseEntity.ok(ApiResponse.error(message));
    }
    
    /**
     * 返回带状态码和消息的失败响应
     * @param <T> 数据类型
     * @param code 状态码
     * @param message 消息
     * @return ResponseEntity对象
     */
    protected <T> ResponseEntity<ApiResponse<T>> error(int code, String message) {
        return ResponseEntity.ok(ApiResponse.error(code, message));
    }
} 
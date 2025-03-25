package com.dbmanage.api.controller;

import com.dbmanage.api.dto.UserDetailsImpl;
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
} 
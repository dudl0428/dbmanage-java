package com.dbmanage.api.dto.auth;

/**
 * 认证响应DTO
 */
public class AuthResponse {
    
    /**
     * JWT令牌
     */
    private String accessToken;
    
    /**
     * 令牌类型
     */
    private String type = "Bearer";
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 构造函数
     */
    public AuthResponse(String accessToken, String type, Long id, String username, String email, String role) {
        this.accessToken = accessToken;
        this.type = type;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    /**
     * 构造函数 (无类型参数版本)
     */
    public AuthResponse(String accessToken, Long id, String username, String email, String role) {
        this.accessToken = accessToken;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String token) {
        this.accessToken = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
} 
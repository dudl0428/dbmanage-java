package com.dbmanage.api.dto.connection;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 数据库连接测试请求DTO
 */
public class ConnectionTestRequest {

    @NotBlank(message = "数据库类型不能为空")
    // 添加type字段，前端传递的字段
    private String type;
    
    @NotBlank(message = "主机地址不能为空")
    private String host;
    
    @NotNull(message = "端口号不能为空")
    private Integer port;
    
    private String database;
    
    private String username;
    
    private String password;
    
    private String parameters;
    
    // Getters and Setters
    

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
} 
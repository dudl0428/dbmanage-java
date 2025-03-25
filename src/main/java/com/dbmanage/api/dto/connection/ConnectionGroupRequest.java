package com.dbmanage.api.dto.connection;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 数据库连接分组请求DTO
 */
public class ConnectionGroupRequest {
    
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 100, message = "分组名称长度不能超过100个字符")
    private String name;
    
    @Size(max = 500, message = "分组描述长度不能超过500个字符")
    private String description;
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
} 
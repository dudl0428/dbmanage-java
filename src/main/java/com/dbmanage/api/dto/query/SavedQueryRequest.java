package com.dbmanage.api.dto.query;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 保存查询请求DTO
 */
public class SavedQueryRequest {
    
    @NotBlank(message = "查询名称不能为空")
    private String name;
    
    private String description;
    
    @NotBlank(message = "SQL语句不能为空")
    private String sql;
    
    @NotNull(message = "数据库连接ID不能为空")
    private Long connectionId;
    
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
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public Long getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }
} 
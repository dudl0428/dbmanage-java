package com.dbmanage.api.dto.query;

import java.time.LocalDateTime;

public class SavedQueryResponse {
    
    private Long id;
    private String name;
    private String description;
    private String sql;
    private Long connectionId;
    private String connectionName;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getConnectionName() {
        return connectionName;
    }
    
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }
    
    public String gettype() {
        return type;
    }
    
    public void settype(String type) {
        this.type = type;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 
package com.dbmanage.api.dto.connection;

import com.dbmanage.api.model.ConnectionGroup;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库连接分组响应DTO
 */
public class ConnectionGroupResponse {
    
    private Long id;
    private String name;
    private String description;
    
    @JsonProperty("connection_count")
    private int connectionCount;
    
    @JsonProperty("connections")
    private List<ConnectionResponse> connectionResponses;
    
    @JsonProperty("created_at")
    private Date createdAt;
    
    @JsonProperty("updated_at")
    private Date updatedAt;
    
    public ConnectionGroupResponse() {
    }
    
    public ConnectionGroupResponse(ConnectionGroup group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.connectionCount = group.getConnections().size();
        this.createdAt = group.getCreatedAt();
        this.updatedAt = group.getUpdatedAt();
    }
    
    public ConnectionGroupResponse(ConnectionGroup group, boolean includeConnections) {
        this(group);
        if (includeConnections) {
            this.connectionResponses = group.getConnections().stream()
                    .map(ConnectionResponse::new)
                    .collect(Collectors.toList());
        }
    }
    
    // Getters and Setters
    
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
    
    public int getConnectionCount() {
        return connectionCount;
    }
    
    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }
    
    public List<ConnectionResponse> getConnectionResponses() {
        return connectionResponses;
    }
    
    public void setConnectionResponses(List<ConnectionResponse> connectionResponses) {
        this.connectionResponses = connectionResponses;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 
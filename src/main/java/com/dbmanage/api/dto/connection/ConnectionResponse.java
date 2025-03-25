package com.dbmanage.api.dto.connection;

import com.dbmanage.api.model.DatabaseConnection;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 数据库连接响应DTO
 */
public class ConnectionResponse {

    private Long id;
    private String name;
    private String type;
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String url;
    private String parameters;
    private Date createdAt;
    private Date updatedAt;
    private Date lastConnected;
    @JsonProperty("group_id")
    private Long groupId;
    @JsonProperty("group_name")
    private String groupName;
    
    public ConnectionResponse() {
    }
    
    public ConnectionResponse(DatabaseConnection connection) {
        this.id = connection.getId();
        this.name = connection.getName();
        this.type = connection.getType();
        this.host = connection.getHost();
        this.port = connection.getPort();
        this.database = connection.getDatabase();
        this.username = connection.getUsername();
        this.url = connection.getUrl();
        this.parameters = connection.getParameters();
        this.createdAt = connection.getCreatedAt();
        this.updatedAt = connection.getUpdatedAt();
        this.lastConnected = connection.getLastConnected();
        
        if (connection.getGroup() != null) {
            this.groupId = connection.getGroup().getId();
            this.groupName = connection.getGroup().getName();
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
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
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
    
    public Date getLastConnected() {
        return lastConnected;
    }
    
    public void setLastConnected(Date lastConnected) {
        this.lastConnected = lastConnected;
    }
    
    public Long getGroupId() {
        return groupId;
    }
    
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
} 
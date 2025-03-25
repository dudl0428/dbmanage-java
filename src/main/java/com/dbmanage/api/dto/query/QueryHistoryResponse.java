package com.dbmanage.api.dto.query;

import java.time.LocalDateTime;

public class QueryHistoryResponse {
    
    private Long id;
    private String sql;
    private Long executionTime;
    private Integer affectedRows;
    private boolean success;
    private String errorMessage;
    private Long connectionId;
    private String connectionName;
    private String type;
    private LocalDateTime executedAt;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public Long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }
    
    public Integer getAffectedRows() {
        return affectedRows;
    }
    
    public void setAffectedRows(Integer affectedRows) {
        this.affectedRows = affectedRows;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
} 
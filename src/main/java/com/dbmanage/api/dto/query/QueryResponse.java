package com.dbmanage.api.dto.query;

import java.util.List;
import java.util.Map;

/**
 * 查询响应DTO类
 */
public class QueryResponse {
    
    /**
     * 是否为查询结果（SELECT查询）
     */
    private boolean isQueryResult;
    
    /**
     * 受影响的行数（非SELECT查询）
     */
    private int affectedRows;
    
    /**
     * 查询结果列名（SELECT查询）
     */
    private List<String> columns;
    
    /**
     * 查询结果数据（SELECT查询）
     */
    private List<Map<String, Object>> data;
    
    /**
     * 查询执行时间（毫秒）
     */
    private long executionTime;
    
    /**
     * 查询是否成功
     */
    private boolean success;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    // Getters and Setters
    
    public boolean isQueryResult() {
        return isQueryResult;
    }
    
    public void setQueryResult(boolean queryResult) {
        isQueryResult = queryResult;
    }
    
    public int getAffectedRows() {
        return affectedRows;
    }
    
    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    
    public List<Map<String, Object>> getData() {
        return data;
    }
    
    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
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
} 
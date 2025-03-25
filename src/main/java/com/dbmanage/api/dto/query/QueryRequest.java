package com.dbmanage.api.dto.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 查询请求DTO类
 */
@Data
public class QueryRequest {
    
    /**
     * 数据库连接ID
     */
    @NotNull(message = "连接ID不能为空")
    private Long connectionId;
    
    /**
     * SQL查询语句
     */
    @NotBlank(message = "SQL查询语句不能为空")
    private String sql;

    /**
     * 数据库名称
     */
    private String database;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 额外参数
     */
    private Map<String, Object> params;
    
    /**
     * SQL参数列表
     */
    private List<Object> parameters;
    
    /**
     * 获取连接ID
     */
    public Long getConnectionId() {
        return connectionId;
    }
    
    /**
     * 设置连接ID
     */
    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * 获取SQL查询语句
     */
    public String getSql() {
        return sql;
    }
    
    /**
     * 设置SQL查询语句
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * 获取数据库名称
     */
    public String getDatabase() {
        return database;
    }

    /**
     * 设置数据库名称
     */
    public void setDatabase(String database) {
        this.database = database;
    }
    
    /**
     * 获取表名
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * 设置表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    /**
     * 获取额外参数
     */
    public Map<String, Object> getParams() {
        return params;
    }
    
    /**
     * 设置额外参数
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    /**
     * 获取SQL参数列表
     */
    public List<Object> getParameters() {
        return parameters;
    }
    
    /**
     * 设置SQL参数列表
     */
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
} 
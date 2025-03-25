package com.dbmanage.api.dto;

import java.util.List;
import java.util.Map;

/**
 * 创建表请求实体类
 */
public class CreateTableRequest {
    private String connectionId;
    private String databaseName;
    private String tableName;
    private String databaseType;
    private List<FieldDefinition> fields;
    private List<IndexDefinition> indexes;
    private List<ForeignKeyDefinition> foreignKeys;
    private List<TriggerDefinition> triggers;
    private Map<String, String> options;
    private String comment;
    
    public String getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    public List<FieldDefinition> getFields() {
        return fields;
    }
    
    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields;
    }
    
    public List<IndexDefinition> getIndexes() {
        return indexes;
    }
    
    public void setIndexes(List<IndexDefinition> indexes) {
        this.indexes = indexes;
    }
    
    public List<ForeignKeyDefinition> getForeignKeys() {
        return foreignKeys;
    }
    
    public void setForeignKeys(List<ForeignKeyDefinition> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
    
    public List<TriggerDefinition> getTriggers() {
        return triggers;
    }
    
    public void setTriggers(List<TriggerDefinition> triggers) {
        this.triggers = triggers;
    }
    
    public Map<String, String> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
    }
    
    @Override
    public String toString() {
        return "CreateTableRequest{" +
                "connectionId='" + connectionId + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", databaseType='" + databaseType + '\'' +
                ", fields=" + fields +
                ", indexes=" + indexes +
                ", foreignKeys=" + foreignKeys +
                ", triggers=" + triggers +
                ", comment='" + comment + '\'' +
                '}';
    }
} 
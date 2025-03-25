package com.dbmanage.api.dto;

/**
 * 验证字段请求实体类
 */
public class ValidateFieldRequest {
    private FieldDefinition field;
    private String databaseType;
    private Long connectionId;
    
    public FieldDefinition getField() {
        return field;
    }
    
    public void setField(FieldDefinition field) {
        this.field = field;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    public Long getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }
    
    @Override
    public String toString() {
        return "ValidateFieldRequest{" +
                "field=" + field +
                ", databaseType='" + databaseType + '\'' +
                ", connectionId='" + connectionId + '\'' +
                '}';
    }
} 
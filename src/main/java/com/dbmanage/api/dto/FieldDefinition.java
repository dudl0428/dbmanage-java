package com.dbmanage.api.dto;

/**
 * 字段定义实体类
 */
public class FieldDefinition {
    private String name;
    private String type;
    private Integer length;
    private Integer decimal;
    private boolean notNull;
    private boolean primaryKey;
    private boolean autoIncrement;
    private String defaultValue;
    private String comment;
    
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
    
    public Integer getLength() {
        return length;
    }
    
    public void setLength(Integer length) {
        this.length = length;
    }
    
    public Integer getDecimal() {
        return decimal;
    }
    
    public void setDecimal(Integer decimal) {
        this.decimal = decimal;
    }
    
    public boolean isNotNull() {
        return notNull;
    }
    
    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }
    
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
    
    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public String toString() {
        return "FieldDefinition{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", length=" + length +
                ", decimal=" + decimal +
                ", notNull=" + notNull +
                ", primaryKey=" + primaryKey +
                ", autoIncrement=" + autoIncrement +
                ", defaultValue='" + defaultValue + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
} 
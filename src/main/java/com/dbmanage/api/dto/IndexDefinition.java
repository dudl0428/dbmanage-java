package com.dbmanage.api.dto;

import java.util.List;

/**
 * 索引定义实体类
 */
public class IndexDefinition {
    private String name;
    private List<String> columnNames;
    private String type;
    private String method;
    private boolean unique;
    private String comment;
    private String databaseType;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public boolean isUnique() {
        return unique;
    }
    
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    @Override
    public String toString() {
        return "IndexDefinition{" +
                "name='" + name + '\'' +
                ", columnNames=" + columnNames +
                ", type='" + type + '\'' +
                ", method='" + method + '\'' +
                ", unique=" + unique +
                ", comment='" + comment + '\'' +
                ", databaseType='" + databaseType + '\'' +
                '}';
    }
} 
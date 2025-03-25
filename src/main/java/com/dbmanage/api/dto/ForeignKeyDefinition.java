package com.dbmanage.api.dto;

import java.util.List;

/**
 * 外键定义实体类
 */
public class ForeignKeyDefinition {
    private String name;
    private List<String> sourceColumns;
    private String referenceTable;
    private List<String> referenceColumns;
    private String updateRule;
    private String deleteRule;
    private String databaseType;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getSourceColumns() {
        return sourceColumns;
    }
    
    public void setSourceColumns(List<String> sourceColumns) {
        this.sourceColumns = sourceColumns;
    }
    
    public String getReferenceTable() {
        return referenceTable;
    }
    
    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }
    
    public List<String> getReferenceColumns() {
        return referenceColumns;
    }
    
    public void setReferenceColumns(List<String> referenceColumns) {
        this.referenceColumns = referenceColumns;
    }
    
    public String getUpdateRule() {
        return updateRule;
    }
    
    public void setUpdateRule(String updateRule) {
        this.updateRule = updateRule;
    }
    
    public String getDeleteRule() {
        return deleteRule;
    }
    
    public void setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    @Override
    public String toString() {
        return "ForeignKeyDefinition{" +
                "name='" + name + '\'' +
                ", sourceColumns=" + sourceColumns +
                ", referenceTable='" + referenceTable + '\'' +
                ", referenceColumns=" + referenceColumns +
                ", updateRule='" + updateRule + '\'' +
                ", deleteRule='" + deleteRule + '\'' +
                ", databaseType='" + databaseType + '\'' +
                '}';
    }
} 
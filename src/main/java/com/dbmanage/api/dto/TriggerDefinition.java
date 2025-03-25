package com.dbmanage.api.dto;

/**
 * 触发器定义实体类
 */
public class TriggerDefinition {
    private String name;
    private String timing;
    private String event;
    private String statement;
    private String databaseType;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTiming() {
        return timing;
    }
    
    public void setTiming(String timing) {
        this.timing = timing;
    }
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getStatement() {
        return statement;
    }
    
    public void setStatement(String statement) {
        this.statement = statement;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    @Override
    public String toString() {
        return "TriggerDefinition{" +
                "name='" + name + '\'' +
                ", timing='" + timing + '\'' +
                ", event='" + event + '\'' +
                ", statement='" + statement + '\'' +
                ", databaseType='" + databaseType + '\'' +
                '}';
    }
} 
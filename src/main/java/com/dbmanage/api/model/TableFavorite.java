package com.dbmanage.api.model;

import javax.persistence.*;
import java.util.Date;

/**
 * 表收藏实体类
 * 用于存储用户收藏的数据库表信息
 */
@Entity
@Table(name = "table_favorites")
public class TableFavorite {

    /**
     * 收藏ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 表名，不能为空，最大长度100
     */
    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;
    
    /**
     * 模式名，最大长度100
     */
    @Column(name = "schema_name", length = 100)
    private String schemaName;
    
    /**
     * 表描述，最大长度500
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 关联的数据库连接，多对一关系
     */
    @ManyToOne
    @JoinColumn(name = "connection_id", nullable = false)
    private DatabaseConnection connection;
    
    /**
     * 关联的用户，多对一关系
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    /**
     * 实体创建前的回调方法
     * 设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public DatabaseConnection getConnection() {
        return connection;
    }
    
    public void setConnection(DatabaseConnection connection) {
        this.connection = connection;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
} 
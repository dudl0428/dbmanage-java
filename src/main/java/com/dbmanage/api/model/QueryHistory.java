package com.dbmanage.api.model;

import javax.persistence.*;
import java.util.Date;

/**
 * 查询历史实体类
 * 用于记录用户执行的SQL查询历史
 */
@Entity
@Table(name = "query_history")
public class QueryHistory {

    /**
     * 历史记录ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * SQL查询文本，不能为空
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sql;

    @Column(name = "query_text", columnDefinition = "TEXT")
    private String queryText;
    
    /**
     * 查询执行时间（毫秒）
     */
    @Column(name = "execution_time")
    private Long executionTime;
    
    /**
     * 影响的行数
     */
    @Column(name = "affected_rows")
    private Integer affectedRows;
    
    /**
     * 查询是否成功，默认为true
     */
    @Column(name = "is_success")
    private Boolean isSuccess;
    
    /**
     * 错误信息，可以为空
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 是否收藏，默认为false
     */
    @Column(name = "favorite")
    private Boolean favorite = false;
    
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
     * 查询执行时间
     */
    @Column(name = "executed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date executedAt;
    
    /**
     * 记录创建时间
     */
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    /**
     * 实体创建前的回调方法
     * 设置创建时间和执行时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        executedAt = new Date();
    }
    
    // Getters and Setters
    
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
    
    public Boolean getIsSuccess() {
        return isSuccess;
    }
    
    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Boolean getFavorite() {
        return favorite;
    }
    
    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
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
    
    public Date getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(Date executedAt) {
        this.executedAt = executedAt;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
} 
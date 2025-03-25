package com.dbmanage.api.model;

import javax.persistence.*;
import java.util.Date;

/**
 * 操作日志实体类
 * 用于记录用户的所有操作历史
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * 日志ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作类型，不能为空，最大长度50
     * 可选值：LOGIN, LOGOUT, QUERY, EXPORT, IMPORT, CREATE, UPDATE, DELETE
     */
    @Column(nullable = false, length = 50)
    private String action;
    
    /**
     * 操作详情
     */
    @Column(name = "action_details", columnDefinition = "TEXT")
    private String actionDetails;
    
    /**
     * 资源名称，最大长度100
     */
    @Column(length = 100)
    private String resource;
    
    /**
     * IP地址，最大长度50
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    /**
     * 用户代理，最大长度200
     */
    @Column(name = "user_agent", length = 200)
    private String userAgent;
    
    /**
     * 关联的用户，多对一关系
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
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
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getActionDetails() {
        return actionDetails;
    }
    
    public void setActionDetails(String actionDetails) {
        this.actionDetails = actionDetails;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
package com.dbmanage.api.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "data_tasks")
public class DataTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String type;  // IMPORT, EXPORT
    
    @Column(nullable = false, length = 20)
    private String status;  // PENDING, RUNNING, COMPLETED, FAILED
    
    @Column(name = "file_name", length = 200)
    private String fileName;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(name = "table_name", length = 100)
    private String tableName;
    
    @Column(name = "total_rows")
    private Long totalRows;
    
    @Column(name = "processed_rows")
    private Long processedRows;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage;
    
    @ManyToOne
    @JoinColumn(name = "connection_id", nullable = false)
    private DatabaseConnection connection;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "started_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;
    
    @Column(name = "completed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        status = "PENDING";
        processedRows = 0L;
        progressPercentage = 0;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public Long getTotalRows() {
        return totalRows;
    }
    
    public void setTotalRows(Long totalRows) {
        this.totalRows = totalRows;
    }
    
    public Long getProcessedRows() {
        return processedRows;
    }
    
    public void setProcessedRows(Long processedRows) {
        this.processedRows = processedRows;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
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
    
    public Date getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }
    
    public Date getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
} 
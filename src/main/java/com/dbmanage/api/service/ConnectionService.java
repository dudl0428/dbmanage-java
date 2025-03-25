package com.dbmanage.api.service;

import com.dbmanage.api.exception.DatabaseConnectionException;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库连接服务
 * 负责管理数据库连接
 */
@Service
public class ConnectionService {
    // 存储连接信息的缓存
    private final Map<String, ConnectionInfo> connectionCache = new ConcurrentHashMap<>();

    /**
     * 获取数据库连接
     * @param connectionId 连接ID
     * @return 数据库连接
     * @throws SQLException 连接失败时抛出
     */
    public Connection getConnection(String connectionId) throws SQLException {
        ConnectionInfo connectionInfo = connectionCache.get(connectionId);
        if (connectionInfo == null) {
            //查询数据库
            throw new DatabaseConnectionException("找不到连接信息，connectionId: " + connectionId);
        }
        
        try {
            // 使用缓存的连接信息建立连接
            return DriverManager.getConnection(
                connectionInfo.getUrl(),
                connectionInfo.getUsername(),
                connectionInfo.getPassword()
            );
        } catch (SQLException e) {
            throw new DatabaseConnectionException("连接数据库失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 测试连接
     * @param connectionInfo 连接信息
     * @return 是否连接成功
     */
    public boolean testConnection(ConnectionInfo connectionInfo) {
        try (Connection conn = DriverManager.getConnection(
                connectionInfo.getUrl(),
                connectionInfo.getUsername(),
                connectionInfo.getPassword())) {
            return conn.isValid(5); // 5秒超时
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 缓存连接信息
     * @param connectionId 连接ID
     * @param connectionInfo 连接信息
     */
    public void cacheConnection(String connectionId, ConnectionInfo connectionInfo) {
        connectionCache.put(connectionId, connectionInfo);
    }
    
    /**
     * 从缓存中移除连接信息
     * @param connectionId 连接ID
     */
    public void removeConnection(String connectionId) {
        connectionCache.remove(connectionId);
    }
    
    /**
     * 数据库连接信息
     */
    public static class ConnectionInfo {
        private String url;
        private String username;
        private String password;
        private String databaseType;
        
        public ConnectionInfo() {
        }
        
        public ConnectionInfo(String url, String username, String password, String databaseType) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.databaseType = databaseType;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getDatabaseType() {
            return databaseType;
        }
        
        public void setDatabaseType(String databaseType) {
            this.databaseType = databaseType;
        }
    }
} 
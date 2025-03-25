package com.dbmanage.api.service;

import com.dbmanage.api.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接服务接口
 * 负责管理数据库连接
 */
public interface ConnectionService {
    /**
     * 获取数据库连接
     * @param connectionId 连接ID
     * @return 数据库连接
     * @throws SQLException 连接失败时抛出
     */
    Connection getConnection(Long connectionId) throws SQLException;

    /**
     * 测试连接
     * @param connectionInfo 连接信息
     * @return 是否连接成功
     */
    boolean testConnection(DatabaseConnection connectionInfo);

    /**
     * 缓存连接信息
     * @param connectionId 连接ID
     * @param connectionInfo 连接信息
     */
    void cacheConnection(Long connectionId, DatabaseConnection connectionInfo);

    /**
     * 从缓存中移除连接信息
     * @param connectionId 连接ID
     */
    void removeConnection(Long connectionId);
} 
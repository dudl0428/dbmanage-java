package com.dbmanage.api.service;

import com.dbmanage.api.dto.connection.ConnectionRequest;
import com.dbmanage.api.dto.connection.ConnectionResponse;
import com.dbmanage.api.dto.connection.ConnectionTestRequest;
import com.dbmanage.api.model.DatabaseConnection;

import java.util.List;
import java.util.Map;

/**
 * 数据库连接服务接口
 */
public interface DatabaseConnectionService {
    
    /**
     * 创建数据库连接
     * @param userId 用户ID
     * @param request 连接请求
     * @return 连接响应
     */
    ConnectionResponse createConnection(Long userId, ConnectionRequest request);
    
    /**
     * 更新数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @param request 连接请求
     * @return 连接响应
     */
    ConnectionResponse updateConnection(Long userId, Long connectionId, ConnectionRequest request);
    
    /**
     * 获取数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @return 连接响应
     */
    ConnectionResponse getConnection(Long userId, Long connectionId);
    
    /**
     * 获取用户的所有数据库连接
     * @param userId 用户ID
     * @return 连接响应列表
     */
    List<ConnectionResponse> getUserConnections(Long userId);
    
    /**
     * 删除数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @return 是否成功
     */
    boolean deleteConnection(Long userId, Long connectionId);
    
    /**
     * 测试数据库连接
     * @param request 连接测试请求
     * @return 测试结果
     */
    Map<String, Object> testConnection(ConnectionTestRequest request);
    
    /**
     * 根据连接ID打开数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @return 连接结果
     */
    Map<String, Object> openConnection(Long userId, Long connectionId);
} 
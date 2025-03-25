package com.dbmanage.api.service;

import com.dbmanage.api.dto.connection.ConnectionGroupRequest;
import com.dbmanage.api.dto.connection.ConnectionGroupResponse;

import java.util.List;

/**
 * 数据库连接分组服务接口
 */
public interface ConnectionGroupService {
    
    /**
     * 创建数据库连接分组
     * @param userId 用户ID
     * @param request 分组请求
     * @return 分组响应
     */
    ConnectionGroupResponse createGroup(Long userId, ConnectionGroupRequest request);
    
    /**
     * 更新数据库连接分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param request 分组请求
     * @return 分组响应
     */
    ConnectionGroupResponse updateGroup(Long userId, Long groupId, ConnectionGroupRequest request);
    
    /**
     * 获取数据库连接分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param includeConnections 是否包含连接信息
     * @return 分组响应
     */
    ConnectionGroupResponse getGroup(Long userId, Long groupId, boolean includeConnections);
    
    /**
     * 获取用户的所有数据库连接分组
     * @param userId 用户ID
     * @return 分组响应列表
     */
    List<ConnectionGroupResponse> getUserGroups(Long userId);
    
    /**
     * 删除数据库连接分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @return 是否成功
     */
    boolean deleteGroup(Long userId, Long groupId);
    
    /**
     * 将连接添加到分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param connectionId 连接ID
     * @return 分组响应
     */
    ConnectionGroupResponse addConnectionToGroup(Long userId, Long groupId, Long connectionId);
    
    /**
     * 从分组中移除连接
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param connectionId 连接ID
     * @return 分组响应
     */
    ConnectionGroupResponse removeConnectionFromGroup(Long userId, Long groupId, Long connectionId);
} 
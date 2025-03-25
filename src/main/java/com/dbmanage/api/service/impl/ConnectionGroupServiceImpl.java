package com.dbmanage.api.service.impl;

import com.dbmanage.api.dto.connection.ConnectionGroupRequest;
import com.dbmanage.api.dto.connection.ConnectionGroupResponse;
import com.dbmanage.api.exception.ResourceNotFoundException;
import com.dbmanage.api.model.ConnectionGroup;
import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.User;
import com.dbmanage.api.repository.ConnectionGroupRepository;
import com.dbmanage.api.repository.DatabaseConnectionRepository;
import com.dbmanage.api.repository.UserRepository;
import com.dbmanage.api.service.ConnectionGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库连接分组服务实现
 */
@Service
public class ConnectionGroupServiceImpl implements ConnectionGroupService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionGroupServiceImpl.class);
    
    @Autowired
    private ConnectionGroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DatabaseConnectionRepository connectionRepository;
    
    /**
     * 创建数据库连接分组
     * @param userId 用户ID
     * @param request 分组请求
     * @return 分组响应
     */
    @Override
    @Transactional
    public ConnectionGroupResponse createGroup(Long userId, ConnectionGroupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        if (groupRepository.existsByUserAndName(user, request.getName())) {
            throw new IllegalArgumentException("Group with name " + request.getName() + " already exists");
        }
        
        ConnectionGroup group = new ConnectionGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setUser(user);
        
        ConnectionGroup savedGroup = groupRepository.save(group);
        
        return new ConnectionGroupResponse(savedGroup);
    }
    
    /**
     * 更新数据库连接分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param request 分组请求
     * @return 分组响应
     */
    @Override
    @Transactional
    public ConnectionGroupResponse updateGroup(Long userId, Long groupId, ConnectionGroupRequest request) {
        ConnectionGroup group = getGroupWithUserCheck(userId, groupId);
        
        // 检查新名称是否已存在
        if (!group.getName().equals(request.getName()) && 
            groupRepository.existsByUserAndName(group.getUser(), request.getName())) {
            throw new IllegalArgumentException("Group with name " + request.getName() + " already exists");
        }
        
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        
        ConnectionGroup updatedGroup = groupRepository.save(group);
        
        return new ConnectionGroupResponse(updatedGroup);
    }
    
    /**
     * 获取数据库连接分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param includeConnections 是否包含连接信息
     * @return 分组响应
     */
    @Override
    public ConnectionGroupResponse getGroup(Long userId, Long groupId, boolean includeConnections) {
        ConnectionGroup group = getGroupWithUserCheck(userId, groupId);
        return new ConnectionGroupResponse(group, includeConnections);
    }
    
    /**
     * 获取用户的所有数据库连接分组
     * @param userId 用户ID
     * @return 分组响应列表
     */
    @Override
    public List<ConnectionGroupResponse> getUserGroups(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        List<ConnectionGroup> groups = groupRepository.findByUser(user);
        
        return groups.stream()
                .map(ConnectionGroupResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除数据库连接分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteGroup(Long userId, Long groupId) {
        ConnectionGroup group = getGroupWithUserCheck(userId, groupId);
        
        // 设置所有属于该分组的连接的分组为null
        for (DatabaseConnection connection : group.getConnections()) {
            connection.setGroup(null);
            connectionRepository.save(connection);
        }
        
        groupRepository.delete(group);
        return true;
    }
    
    /**
     * 将连接添加到分组
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param connectionId 连接ID
     * @return 分组响应
     */
    @Override
    @Transactional
    public ConnectionGroupResponse addConnectionToGroup(Long userId, Long groupId, Long connectionId) {
        ConnectionGroup group = getGroupWithUserCheck(userId, groupId);
        
        DatabaseConnection connection = connectionRepository.findByIdAndUser(connectionId, group.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
        
        connection.setGroup(group);
        connectionRepository.save(connection);
        
        // 重新获取分组以确保连接列表已更新
        ConnectionGroup updatedGroup = groupRepository.findById(groupId).get();
        
        return new ConnectionGroupResponse(updatedGroup, true);
    }
    
    /**
     * 从分组中移除连接
     * @param userId 用户ID
     * @param groupId 分组ID
     * @param connectionId 连接ID
     * @return 分组响应
     */
    @Override
    @Transactional
    public ConnectionGroupResponse removeConnectionFromGroup(Long userId, Long groupId, Long connectionId) {
        ConnectionGroup group = getGroupWithUserCheck(userId, groupId);
        
        DatabaseConnection connection = connectionRepository.findByIdAndUser(connectionId, group.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
        
        if (connection.getGroup() != null && connection.getGroup().getId().equals(groupId)) {
            connection.setGroup(null);
            connectionRepository.save(connection);
        } else {
            throw new IllegalArgumentException("Connection is not in the specified group");
        }
        
        // 重新获取分组以确保连接列表已更新
        ConnectionGroup updatedGroup = groupRepository.findById(groupId).get();
        
        return new ConnectionGroupResponse(updatedGroup, true);
    }
    
    /**
     * 获取分组并检查用户权限
     * @param userId 用户ID
     * @param groupId 分组ID
     * @return 分组
     */
    private ConnectionGroup getGroupWithUserCheck(Long userId, Long groupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        return groupRepository.findByIdAndUser(groupId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId + " for user"));
    }
} 
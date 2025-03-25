package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.common.BaseController;
import com.dbmanage.api.dto.connection.ConnectionGroupRequest;
import com.dbmanage.api.dto.connection.ConnectionGroupResponse;
import com.dbmanage.api.service.ConnectionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 数据库连接分组控制器
 */
@RestController
@RequestMapping("/connection-groups")
public class ConnectionGroupController extends BaseController {

    @Autowired
    private ConnectionGroupService groupService;
    
    /**
     * 创建数据库连接分组
     * @param request 分组请求
     * @return 分组响应
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConnectionGroupResponse>> createGroup(@Valid @RequestBody ConnectionGroupRequest request) {
        Long userId = getCurrentUserId();
        ConnectionGroupResponse response = groupService.createGroup(userId, request);
        return success( response);
    }
    
    /**
     * 更新数据库连接分组
     * @param id 分组ID
     * @param request 分组请求
     * @return 分组响应
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConnectionGroupResponse>> updateGroup(
            @PathVariable Long id, 
            @Valid @RequestBody ConnectionGroupRequest request) {
        Long userId = getCurrentUserId();
        ConnectionGroupResponse response = groupService.updateGroup(userId, id, request);
        return success( response);
    }
    
    /**
     * 获取数据库连接分组
     * @param id 分组ID
     * @param includeConnections 是否包含连接信息
     * @return 分组响应
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConnectionGroupResponse>> getGroup(
            @PathVariable Long id,
            @RequestParam(value = "include_connections", defaultValue = "false") boolean includeConnections) {
        Long userId = getCurrentUserId();
        ConnectionGroupResponse response = groupService.getGroup(userId, id, includeConnections);
        return success(response);
    }
    
    /**
     * 获取用户的所有数据库连接分组
     * @return 分组响应列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConnectionGroupResponse>>> getUserGroups() {
        Long userId = getCurrentUserId();
        List<ConnectionGroupResponse> groups = groupService.getUserGroups(userId);
        return success(groups);
    }
    
    /**
     * 删除数据库连接分组
     * @param id 分组ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteGroup(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        boolean result = groupService.deleteGroup(userId, id);
        if (result) {
            return success( true);
        } else {
            return error("删除分组失败");
        }
    }
    
    /**
     * 将连接添加到分组
     * @param id 分组ID
     * @param connectionId 连接ID
     * @return 分组响应
     */
    @PostMapping("/{id}/connections/{connectionId}")
    public ResponseEntity<ApiResponse<ConnectionGroupResponse>> addConnectionToGroup(
            @PathVariable Long id,
            @PathVariable Long connectionId) {
        Long userId = getCurrentUserId();
        ConnectionGroupResponse response = groupService.addConnectionToGroup(userId, id, connectionId);
        return success(response);
    }
    
    /**
     * 从分组中移除连接
     * @param id 分组ID
     * @param connectionId 连接ID
     * @return 分组响应
     */
    @DeleteMapping("/{id}/connections/{connectionId}")
    public ResponseEntity<ApiResponse<ConnectionGroupResponse>> removeConnectionFromGroup(
            @PathVariable Long id,
            @PathVariable Long connectionId) {
        Long userId = getCurrentUserId();
        ConnectionGroupResponse response = groupService.removeConnectionFromGroup(userId, id, connectionId);
        return success(response);
    }
} 
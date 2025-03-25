package com.dbmanage.api.service;

import com.dbmanage.api.dto.query.QueryHistoryResponse;
import com.dbmanage.api.dto.query.QueryRequest;
import com.dbmanage.api.dto.query.QueryResponse;
import com.dbmanage.api.dto.query.SavedQueryResponse;

import java.util.List;
import java.util.Map;

/**
 * 查询服务接口
 */
public interface QueryService {
    
    /**
     * 执行SQL查询
     * 
     * @param request 查询请求
     * @return 查询响应
     */
    QueryResponse executeQuery(QueryRequest request);
    
    /**
     * 获取数据库结构信息
     * 
     * @param connectionId 数据库连接ID
     * @return 数据库结构信息
     */
    List<Map<String, Object>> getDatabaseStructure(Long connectionId);
    
    /**
     * 获取表结构信息
     * 
     * @param connectionId 数据库连接ID
     * @param tableName 表名
     * @return 表结构信息
     */
    List<Map<String, Object>> getTableStructure(Long connectionId, String tableName);
    
    /**
     * 保存查询
     * 
     * @param name 查询名称
     * @param description 查询描述
     * @param sql SQL语句
     * @param connectionId 数据库连接ID
     * @param userId 用户ID
     * @return 保存结果
     */
    boolean saveQuery(String name, String description, String sql, Long connectionId, Long userId);
    
    /**
     * 获取用户保存的查询列表
     * 
     * @param userId 用户ID
     * @return 查询列表
     */
    List<SavedQueryResponse> getSavedQueries(Long userId);
    
    /**
     * 获取用户查询历史
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 查询历史列表
     */
    List<QueryHistoryResponse> getQueryHistory(Long userId, int limit);
    
    /**
     * 获取数据库表列表
     * 
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 表列表信息
     */
    List<Map<String, Object>> getDatabaseTables(Long connectionId, String database, String schemaName);
    
    /**
     * 获取数据库视图列表
     * 
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 视图列表信息
     */
    List<Map<String, Object>> getDatabaseViews(Long connectionId, String database, String schemaName);
    
    /**
     * 获取数据库函数列表
     * 
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 函数列表信息
     */
    List<Map<String, Object>> getDatabaseFunctions(Long connectionId, String database, String schemaName);
    
    /**
     * 获取数据库事件列表
     * 
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 事件列表信息
     */
    List<Map<String, Object>> getDatabaseEvents(Long connectionId, String database, String schemaName);
} 
package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.common.BaseController;
import com.dbmanage.api.dto.query.QueryRequest;
import com.dbmanage.api.dto.query.QueryResponse;
import com.dbmanage.api.service.DatabaseConnectionService;
import com.dbmanage.api.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 数据库结构API控制器
 */
@RestController
@RequestMapping("/database")
public class DatabaseSchemaController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaController.class);

    @Autowired
    private QueryService queryService;

    @Autowired
    private DatabaseConnectionService connectionService;
    
    // 用于并行处理的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 获取数据库结构信息
     * @param connectionId 连接ID，如果不提供则返回所有用户连接的结构
     * @return 数据库结构信息
     */
    @GetMapping("/schema")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDatabaseSchema(
            @RequestParam(required = false) Long connectionId) {
        try {
            Long userId = getCurrentUserId();
            List<Map<String, Object>> result = new ArrayList<>();

            // 如果没有提供连接ID，则返回用户所有连接的结构
            if (connectionId == null) {
                // 获取用户的所有连接
                List<Map<String, Object>> userConnections = new ArrayList<>();
                connectionService.getUserConnections(userId).forEach(conn -> {
                    Map<String, Object> connection = new HashMap<>();
                    connection.put("id", conn.getId());
                    connection.put("name", conn.getName());
                    connection.put("type", conn.getType());
                    
                    // 尝试获取该连接的数据库结构
                    try {
                        List<Map<String, Object>> structure = queryService.getDatabaseStructure(conn.getId());
                        connection.put("databases", structure);
                    } catch (Exception e) {
                        logger.error("获取连接 {} 的数据库结构失败: {}", conn.getId(), e.getMessage(), e);
                        connection.put("error", "无法获取数据库结构: " + e.getMessage());
                    }
                    
                    userConnections.add(connection);
                });
                
                // 返回所有连接的结构
                Map<String, Object> rootNode = new HashMap<>();
                rootNode.put("label", "数据库连接");
                rootNode.put("value", "connections");
                rootNode.put("children", userConnections);
                result.add(rootNode);
            } else {
                // 返回特定连接的结构
                result = queryService.getDatabaseStructure(connectionId);
            }
            
            return success(result);
        } catch (Exception e) {
            logger.error("获取数据库结构失败: {}", e.getMessage(), e);
            return error("获取数据库结构失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取特定数据库的表结构信息
     * @param connectionId 连接ID
     * @param database 数据库名称
     * @return 表结构信息
     */
    @GetMapping("/schema/{connectionId}/{database}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabaseTables(
            @PathVariable Long connectionId,
            @PathVariable String database) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取表列表
            List<Map<String, Object>> tables = queryService.getDatabaseTables(connectionId, database, null);
            result.put("tables", tables);
            
            // 获取视图列表
            List<Map<String, Object>> views = queryService.getDatabaseViews(connectionId, database, null);
            result.put("views", views);
            
            // 获取函数列表
            List<Map<String, Object>> functions = queryService.getDatabaseFunctions(connectionId, database, null);
            result.put("functions", functions);
            
            // 获取事件列表
            List<Map<String, Object>> events = queryService.getDatabaseEvents(connectionId, database, null);
            result.put("events", events);
            
            return success(result);
        } catch (Exception e) {
            logger.error("获取数据库 {} 的表结构失败: {}", database, e.getMessage(), e);
            return error("获取数据库表结构失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取表结构详情
     * @param connectionId 连接ID
     * @param database 数据库名称
     * @param table 表名
     * @return 表结构详情
     */
    @GetMapping("/schema/{connectionId}/{database}/{table}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableSchema(
            @PathVariable Long connectionId,
            @PathVariable String database,
            @PathVariable String table) {
        try {
            // 获取表结构详情，确保传递数据库名称
            // 需要查看QueryService的实现，可能需要修改接口以支持数据库名称参数
            List<Map<String, Object>> tableStructure = queryService.getTableStructure(connectionId, database + "." + table);
            return success(tableStructure);
        } catch (Exception e) {
            logger.error("获取表 {}.{} 的结构详情失败: {}", database, table, e.getMessage(), e);
            return error("获取表结构详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取完整的数据库结构，包括所有表、列及其关系等信息
     * 专门用于AI自然语言转SQL功能，提供足够的信息帮助AI理解数据库结构
     * 
     * @param connectionId 连接ID
     * @return 包含详细数据库结构的完整信息
     */
    @GetMapping("/complete-schema/{connectionId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCompleteSchema(@PathVariable Long connectionId) {
        try {
           Integer  limit=100;
            Map<String, Object> result = new HashMap<>();
            
            // 获取连接信息
            connectionService.getUserConnections(getCurrentUserId()).stream()
                .filter(conn -> conn.getId().equals(connectionId))
                .findFirst()
                .ifPresent(conn -> {
                    result.put("connectionId", conn.getId());
                    result.put("connectionName", conn.getName());
                    result.put("databaseType", conn.getType());
                    result.put("host", conn.getHost());
                    result.put("port", conn.getPort());
                    result.put("defaultDatabase", conn.getDatabase());
                });
            
            // 获取所有数据库
            List<Map<String, Object>> databaseStructure = queryService.getDatabaseStructure(connectionId);
            result.put("databases", databaseStructure);
            
            // 获取每个数据库的详细信息
            List<Map<String, Object>> detailedDatabases = new ArrayList<>();
            
            // 从数据库结构中提取数据库名称
            for (Map<String, Object> database : databaseStructure) {
                String databaseName = (String) database.get("label");
                Map<String, Object> detailedDatabase = new HashMap<>();
                detailedDatabase.put("name", databaseName);
                
                try {
                    // 获取表列表及其结构
                    List<Map<String, Object>> tables = queryService.getDatabaseTables(connectionId, databaseName, null);
                    
                    // 如果表数量过多，只取前limit个
                    if (tables.size() > limit) {
                        logger.info("数据库 {} 的表数量 {} 超过限制 {}，将只返回前 {} 个表",
                                databaseName, tables.size(), limit, limit);
                        tables = tables.subList(0, limit);
                        detailedDatabase.put("truncated", true);
                        detailedDatabase.put("totalTables", tables.size());
                    }
                    
                    // 并行处理表结构获取，提高性能
                    List<CompletableFuture<Map<String, Object>>> tableFutures = tables.stream()
                        .map(table -> {
                            String tableName = (String) table.get("label");
                            return CompletableFuture.supplyAsync(() -> {
                                Map<String, Object> detailedTable = new HashMap<>();
                                detailedTable.put("name", tableName);
                                
                                // 获取表的列信息
                                try {
                                    // 确保传递完整的表名，包括数据库名称
                                    List<Map<String, Object>> columns = queryService.getTableStructure(connectionId, databaseName + "." + tableName);
                                    detailedTable.put("columns", columns);
                                } catch (Exception e) {
                                    logger.error("获取表 {}.{} 的结构失败: {}", databaseName, tableName, e.getMessage(), e);
                                    detailedTable.put("error", "无法获取表结构: " + e.getMessage());
                                }
                                
                                return detailedTable;
                            }, executorService);
                        })
                        .collect(Collectors.toList());
                    
                    // 等待所有表结构获取完成
                    List<Map<String, Object>> detailedTables = tableFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
                    
                    detailedDatabase.put("tables", detailedTables);
                    
                    // 获取视图列表
                    List<Map<String, Object>> views = queryService.getDatabaseViews(connectionId, databaseName, null);
                    detailedDatabase.put("views", views);
                    
                    // 获取函数列表
                    List<Map<String, Object>> functions = queryService.getDatabaseFunctions(connectionId, databaseName, null);
                    detailedDatabase.put("functions", functions);
                    
                    detailedDatabases.add(detailedDatabase);
                } catch (Exception e) {
                    logger.error("获取数据库 {} 的结构失败: {}", databaseName, e.getMessage(), e);
                    detailedDatabase.put("error", "无法获取数据库结构: " + e.getMessage());
                    detailedDatabases.add(detailedDatabase);
                }
            }
            
            result.put("detailedDatabases", detailedDatabases);
            
            return success(result);
        } catch (Exception e) {
            logger.error("获取完整数据库结构失败: {}", e.getMessage(), e);
            return error("获取完整数据库结构失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定数据库的完整结构信息，包括表、视图、字段等
     * 专门用于SQL编辑器的自动补全功能
     * 
     * @param connectionId 连接ID
     * @param database 数据库名称
     * @return 该数据库的完整结构信息
     */
    @GetMapping("/complete-schema")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSpecificDatabaseSchema(
            @RequestParam Long connectionId,
            @RequestParam String database) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取连接信息
            connectionService.getUserConnections(getCurrentUserId()).stream()
                .filter(conn -> conn.getId().equals(connectionId))
                .findFirst()
                .ifPresent(conn -> {
                    result.put("connectionId", conn.getId());
                    result.put("connectionName", conn.getName());
                    result.put("databaseType", conn.getType());
                    result.put("database", database);
                });
            
            // 获取表列表
            List<Map<String, Object>> tables = queryService.getDatabaseTables(connectionId, database, null);
            List<Map<String, Object>> detailedTables = new ArrayList<>();
            
            // 设置合理的限制，避免表过多时性能问题
            int limit = 100;
            if (tables.size() > limit) {
                tables = tables.subList(0, limit);
                result.put("truncated", true);
                result.put("totalTables", tables.size());
            }
            
            // 并行获取表结构信息
            List<CompletableFuture<Map<String, Object>>> tableFutures = tables.stream()
                .map(table -> {
                    String tableName = (String) table.get("label");
                    return CompletableFuture.supplyAsync(() -> {
                        Map<String, Object> detailedTable = new HashMap<>();
                        
                        try {
                            // 获取表的字段信息
                            List<Map<String, Object>> columnsData = queryService.getTableStructure(connectionId, database + "." + tableName);
                            
                            // 表信息处理：分离表元数据和列信息
                            if (!columnsData.isEmpty() && columnsData.get(0).containsKey("type") && "TABLE".equals(columnsData.get(0).get("type"))) {
                                // 第一个元素是表元数据
                                Map<String, Object> tableMetadata = columnsData.get(0);
                                detailedTable.put("name", tableMetadata.get("name"));
                                detailedTable.put("comment", tableMetadata.get("comment") != null ? tableMetadata.get("comment") : "");
                                
                                // 处理列信息，过滤掉表元数据
                                List<Map<String, Object>> columns = columnsData.stream()
                                    .skip(1) // 跳过表元数据
                                    .map(column -> {
                                        Map<String, Object> formattedColumn = new HashMap<>();
                                        formattedColumn.put("name", column.get("name"));
                                        formattedColumn.put("type", column.get("type"));
                                        formattedColumn.put("comment", column.get("comment") != null ? column.get("comment") : "");
                                        formattedColumn.put("nullable", column.get("nullable"));
                                        formattedColumn.put("isPrimaryKey", column.get("isPrimaryKey"));
                                        formattedColumn.put("defaultValue", column.get("defaultValue"));
                                        return formattedColumn;
                                    })
                                    .collect(Collectors.toList());
                                
                                detailedTable.put("columns", columns);
                            } else {
                                // 兼容旧版返回结构（无表元数据的情况）
                                detailedTable.put("name", tableName);
                                detailedTable.put("comment", "");
                                
                                List<Map<String, Object>> columns = columnsData.stream()
                                    .map(column -> {
                                        Map<String, Object> formattedColumn = new HashMap<>();
                                        formattedColumn.put("name", column.get("name"));
                                        formattedColumn.put("type", column.get("type"));
                                        formattedColumn.put("comment", column.get("comment") != null ? column.get("comment") : "");
                                        formattedColumn.put("nullable", column.get("nullable"));
                                        formattedColumn.put("defaultValue", column.get("defaultValue"));
                                        return formattedColumn;
                                    })
                                    .collect(Collectors.toList());
                                
                                detailedTable.put("columns", columns);
                            }
                        } catch (Exception e) {
                            logger.error("获取表 {}.{} 的结构失败: {}", database, tableName, e.getMessage(), e);
                            detailedTable.put("name", tableName);
                            detailedTable.put("error", "无法获取表结构: " + e.getMessage());
                            detailedTable.put("columns", new ArrayList<>());
                        }
                        
                        return detailedTable;
                    }, executorService);
                })
                .collect(Collectors.toList());
            
            // 等待所有表结构获取完成
            detailedTables = tableFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            result.put("tables", detailedTables);
            
            // 获取视图列表
            List<Map<String, Object>> views = queryService.getDatabaseViews(connectionId, database, null);
            result.put("views", views);
            
            return success(result);
        } catch (Exception e) {
            logger.error("获取数据库 {} 的结构失败: {}", database, e.getMessage(), e);
            return error("获取数据库结构失败: " + e.getMessage());
        }
    }

    /**
     * 创建新数据库
     * @param connectionId 连接ID
     * @param request 包含数据库名称的请求体
     * @return 创建结果
     */
    @PostMapping("/connections/{connectionId}/create-database")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDatabase(
        @PathVariable Long connectionId, 
        @RequestBody Map<String, String> request
    ) {
        try {
            Long userId = getCurrentUserId();
            String databaseName = request.get("databaseName");
            
            if (databaseName == null || databaseName.trim().isEmpty()) {
                return error("数据库名称不能为空");
            }
            
            // 构建创建数据库的SQL
            String sql = "CREATE DATABASE `" + databaseName + "`";
            
            // 构建查询请求
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setConnectionId(connectionId);
            queryRequest.setSql(sql);
            
            // 执行SQL
            QueryResponse result = queryService.executeQuery(queryRequest);
            
            if (result.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("databaseName", databaseName);
                data.put("affectedRows", result.getAffectedRows());
                data.put("executionTime", result.getExecutionTime());
                return success("数据库创建成功", data);
            } else {
                return error(result.getErrorMessage());
            }
        } catch (Exception e) {
            logger.error("创建数据库失败", e);
            return error("创建数据库失败: " + e.getMessage());
        }
    }
} 
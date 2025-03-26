package com.dbmanage.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 安全控制器 - 兼容旧API路径
 * 重定向旧的API路径到新的控制器
 */
@RestController
@RequestMapping("/api/connection")
public class SecurityController {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    
    @Autowired
    private QueryService queryService;
    
    /**
     * 获取表结构 - 兼容原API
     */
    @GetMapping("/{connectionId}/schema/{database}/{table}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableStructure(
            @PathVariable Long connectionId,
            @PathVariable String database,
            @PathVariable String table) {
        
        logger.info("旧API路径访问: /api/connection/{}/schema/{}/{}", connectionId, database, table);
        
        try {
            // 构建完整的表名
            String fullTableName = database + "." + table;
            
            // 获取表结构
            List<Map<String, Object>> structure = queryService.getTableStructure(connectionId, fullTableName);
            
            logger.info("成功获取表结构，共 {} 个字段", structure.size());
            return ResponseEntity.ok(ApiResponse.success("获取表结构成功", structure));
            
        } catch (Exception e) {
            logger.error("获取表结构失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "获取表结构失败: " + e.getMessage()));
        }
    }
    
    /**
     * 执行查询 - 兼容原API
     */
    @PostMapping("/{connectionId}/query")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeQuery(
            @PathVariable Long connectionId,
            @RequestBody Map<String, String> request) {
        
        logger.info("旧API路径访问: /api/connection/{}/query", connectionId);
        
        try {
            String database = request.get("database");
            String sql = request.get("sql");
            
            if (database == null || sql == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "数据库名和SQL查询不能为空"));
            }
            
            // 解析SQL，获取相关信息
            boolean isSelectQuery = sql.trim().toLowerCase().startsWith("select");
            String table = extractTableName(sql);
            
            logger.info("执行查询: database={}, isSelect={}, table={}", database, isSelectQuery, table);
            
            // 如果是查询表数据的SELECT语句
            if (isSelectQuery && table != null) {
                // 获取分页信息
                int limit = 100;
                int offset = 0;
                
                // 执行查询
                Map<String, Object> result = queryService.executeQueryWithPagination(
                    connectionId, database, sql, offset, limit);
                
                return ResponseEntity.ok(ApiResponse.success("查询执行成功", result));
            } else {
                // 其他类型的查询
                Map<String, Object> result = queryService.executeGenericQuery(connectionId, database, sql);
                return ResponseEntity.ok(ApiResponse.success("查询执行成功", result));
            }
            
        } catch (Exception e) {
            logger.error("执行查询失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "执行查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从SQL语句中提取表名
     */
    private String extractTableName(String sql) {
        try {
            // 简单匹配: from table_name
            String lowerSql = sql.toLowerCase();
            int fromIndex = lowerSql.indexOf(" from ");
            if (fromIndex < 0) return null;
            
            // 截取from后面的部分
            String fromClause = lowerSql.substring(fromIndex + 6).trim();
            
            // 找到第一个空格、逗号或其他分隔符
            int endIndex = fromClause.indexOf(' ');
            if (endIndex < 0) endIndex = fromClause.indexOf(',');
            if (endIndex < 0) endIndex = fromClause.indexOf(';');
            if (endIndex < 0) endIndex = fromClause.length();
            
            // 提取表名
            String tableName = fromClause.substring(0, endIndex).trim();
            
            // 去除可能的引号或反引号
            return tableName.replaceAll("[`'\"]", "");
        } catch (Exception e) {
            logger.warn("从SQL中提取表名失败: {}", e.getMessage());
            return null;
        }
    }
} 
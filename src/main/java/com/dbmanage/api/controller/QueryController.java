package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.common.BaseController;
import com.dbmanage.api.dto.query.QueryHistoryResponse;
import com.dbmanage.api.dto.query.QueryRequest;
import com.dbmanage.api.dto.query.QueryResponse;
import com.dbmanage.api.dto.query.SavedQueryRequest;
import com.dbmanage.api.dto.query.SavedQueryResponse;
import com.dbmanage.api.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 查询控制器
 */
@RestController
@RequestMapping("/query")
public class QueryController extends BaseController {

    @Autowired
    private QueryService queryService;
    
    /**
     * 执行SQL查询
     * @param request 查询请求
     * @return 查询响应
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<QueryResponse>> executeQuery(@Valid @RequestBody QueryRequest request) {
        QueryResponse response = queryService.executeQuery(request);
        return success(response);
    }
    
    /**
     * 获取数据库结构信息
     * @param connectionId 数据库连接ID
     * @return 数据库结构信息
     */
    @GetMapping("/structure/{connectionId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDatabaseStructure(@PathVariable Long connectionId) {
        List<Map<String, Object>> structure = queryService.getDatabaseStructure(connectionId);
        return success(structure);
    }
    
    /**
     * 获取表结构信息
     * @param connectionId 数据库连接ID
     * @param tableName 表名
     * @return 表结构信息
     */
    @GetMapping("/table-structure")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableStructure(
            @RequestParam Long connectionId, 
            @RequestParam String tableName) {
        List<Map<String, Object>> structure = queryService.getTableStructure(connectionId, tableName);
        return success(structure);
    }
    
    /**
     * 获取数据库表列表
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 表列表信息
     */
    @GetMapping("/tables")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDatabaseTables(
            @RequestParam Long connectionId,
            @RequestParam String database,
            @RequestParam String schemaName) {
        List<Map<String, Object>> tables = queryService.getDatabaseTables(connectionId, database, schemaName);
        return success(tables);
    }
    
    /**
     * 获取数据库视图列表
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 视图列表信息
     */
    @GetMapping("/views")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDatabaseViews(
            @RequestParam Long connectionId,
            @RequestParam String database,
            @RequestParam String schemaName) {
        List<Map<String, Object>> views = queryService.getDatabaseViews(connectionId, database, schemaName);
        return success(views);
    }
    
    /**
     * 保存查询
     * @param request 保存查询请求
     * @return 保存结果
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> saveQuery(@Valid @RequestBody SavedQueryRequest request) {
        Long userId = getCurrentUserId();
        
        boolean saved = queryService.saveQuery(
            request.getName(), 
            request.getDescription(), 
            request.getSql(), 
            request.getConnectionId(), 
            userId
        );
        
        if (saved) {
            return success( true);
        } else {
            return error("查询保存失败");
        }
    }
    
    /**
     * 获取保存的查询列表
     * @return 查询列表
     */
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<SavedQueryResponse>>> getSavedQueries() {
        Long userId = getCurrentUserId();
        List<SavedQueryResponse> queries = queryService.getSavedQueries(userId);
        return success(queries);
    }
    
    /**
     * 获取查询历史
     * @param limit 限制数量
     * @return 查询历史列表
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<QueryHistoryResponse>>> getQueryHistory(@RequestParam(defaultValue = "20") int limit) {
        Long userId = getCurrentUserId();
        List<QueryHistoryResponse> history = queryService.getQueryHistory(userId, limit);
        return success(history);
    }
} 
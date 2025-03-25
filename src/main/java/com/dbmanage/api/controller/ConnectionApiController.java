package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.common.BaseController;
import com.dbmanage.api.dto.connection.ConnectionRequest;
import com.dbmanage.api.dto.connection.ConnectionResponse;
import com.dbmanage.api.dto.connection.ConnectionTestRequest;
import com.dbmanage.api.dto.query.QueryRequest;
import com.dbmanage.api.dto.query.QueryResponse;
import com.dbmanage.api.service.DatabaseConnectionService;
import com.dbmanage.api.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 连接API控制器，提供前后端交互的API
 */
@RestController
@RequestMapping("/connections")
public class ConnectionApiController extends BaseController {

    @Autowired
    private DatabaseConnectionService connectionService;
    
    @Autowired
    private QueryService queryService;

    /**
     * 测试连接
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection(@RequestBody ConnectionTestRequest request) {
        Map<String, Object> result = connectionService.testConnection(request);
        boolean success = (boolean) result.getOrDefault("success", false);
        if (success) {
            return success(result);
        } else {
            String errorMessage = (String) result.getOrDefault("message", "数据库连接失败");
            return error(errorMessage);
        }
    }
    /**
     * 保存连接
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConnectionResponse>> connections(@RequestBody ConnectionRequest request) {
        Long userId = getCurrentUserId();
        ConnectionResponse connection = connectionService.createConnection(userId, request);
        return success(connection);
    }


    /**
     * 获取用户连接
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> connections() {
        Long userId = getCurrentUserId();
        List<ConnectionResponse> connections = connectionService.getUserConnections(userId);
        return success(connections);
    }
    /**
     * 打开数据库连接
     * @param id 连接ID
     * @return 连接结果
     */
    @PostMapping("/{id}/open")
    public ResponseEntity<ApiResponse<Map<String, Object>>> openConnection(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = connectionService.openConnection(userId, id);
        boolean success = (boolean) result.getOrDefault("success", false);
        if (success) {
            return success(result);
        } else {
            String errorMessage = (String) result.getOrDefault("message", "数据库连接失败");
            return error(errorMessage);
        }
    }
    
    /**
     * 关闭数据库连接
     * @param id 连接ID
     * @return 关闭结果
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Map<String, Object>>> closeConnection(@PathVariable Long id) {
        // 实际不需要做任何操作，因为每次查询都会建立新的连接
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "连接已关闭");
        return success(result);
    }
    
    /**
     * 获取连接的数据库列表
     * @param id 连接ID
     * @return 数据库列表
     */
    @GetMapping("/{id}/databases")
    public ResponseEntity<ApiResponse<List<String>>> getDatabases(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            List<Map<String, Object>> databaseStructure = queryService.getDatabaseStructure(id);
            
            // 从结构中提取数据库名称
            List<String> databases = new ArrayList<>();
            for (Map<String, Object> catalog : databaseStructure) {
                if (catalog.containsKey("label")) {
                    databases.add(catalog.get("label").toString());
                }
            }
            
            return success(databases);
        } catch (Exception e) {
            return error("获取数据库列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据库的表列表
     * @param id 连接ID
     * @param database 数据库名称
     * @return 表列表
     */
    @GetMapping("/{id}/databases/{database}/tables")
    public ResponseEntity<ApiResponse<List<String>>> getTables(
            @PathVariable Long id, 
            @PathVariable String database) {
        try {
            // 这里默认使用null作为schemaName，实际使用中可能需要根据数据库类型处理
            List<Map<String, Object>> tables = queryService.getDatabaseTables(id, database, null);
            
            // 从表信息中提取表名
            List<String> tableNames = tables.stream()
                    .map(table -> table.get("label").toString())
                    .collect(Collectors.toList());
            
            return success(tableNames);
        } catch (Exception e) {
            return error("获取表列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据库的视图列表
     * @param id 连接ID
     * @param database 数据库名称
     * @return 视图列表
     */
    @GetMapping("/{id}/databases/{database}/views")
    public ResponseEntity<ApiResponse<List<String>>> getViews(
            @PathVariable Long id, 
            @PathVariable String database) {
        try {
            // 这里默认使用null作为schemaName，实际使用中可能需要根据数据库类型处理
            List<Map<String, Object>> views = queryService.getDatabaseViews(id, database, null);
            
            // 从视图信息中提取视图名
            List<String> viewNames = views.stream()
                    .map(view -> view.get("label").toString())
                    .collect(Collectors.toList());
            
            return success(viewNames);
        } catch (Exception e) {
            return error("获取视图列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取表结构
     * @param id 连接ID
     * @param database 数据库名称
     * @param table 表名
     * @return 表结构信息
     */
    @GetMapping("/{id}/databases/{database}/tables/{table}/schema")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableSchema(
            @PathVariable Long id, 
            @PathVariable String database,
            @PathVariable String table) {
        try {
            List<Map<String, Object>> schema = queryService.getTableStructure(id, table);
            return success(schema);
        } catch (Exception e) {
            return error("获取表结构失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行SQL查询
     * @param id 连接ID
     * @param database 数据库名称
     * @param request 包含SQL的请求体
     * @return 查询结果
     */
    @PostMapping("/{id}/databases/{database}/execute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeQuery(
            @PathVariable Long id, 
            @PathVariable String database,
            @RequestBody Map<String, String> request) {
        try {
            String sql = request.get("sql");
            if (sql == null || sql.trim().isEmpty()) {
                return error("SQL语句不能为空");
            }
            
            // 创建QueryRequest对象
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setConnectionId(id);
            queryRequest.setSql(sql);
            queryRequest.setDatabase(database);
            
            // 如果提供了表名，也设置表名
            String tableName = request.get("table");
            if (tableName != null && !tableName.trim().isEmpty()) {
                queryRequest.setTableName(tableName);
            }
            
            // 判断SQL类型
            String sqlType = determineSqlType(sql.trim().toUpperCase());
            
            // 执行查询并返回结果
            QueryResponse queryResponse = queryService.executeQuery(queryRequest);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", queryResponse.isSuccess());
            
            if (queryResponse.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("sqlType", sqlType);
                data.put("isQueryResult", queryResponse.isQueryResult());
                data.put("columns", queryResponse.getColumns());
                data.put("rows", queryResponse.getData());
                data.put("affectedRows", queryResponse.getAffectedRows());
                data.put("executionTime", queryResponse.getExecutionTime());
                data.put("database", database);
                
                // 为不同类型的SQL提供额外信息
                switch (sqlType) {
                    case "SELECT":
                        // 查询结果已经包含了必要信息
                        break;
                    case "CREATE_TABLE":
                    case "ALTER_TABLE":
                    case "DROP_TABLE":
                        // 对于表操作，返回更新后的表列表
                        try {
                            List<Map<String, Object>> tables = queryService.getDatabaseTables(id, database, null);
                            List<String> tableNames = tables.stream()
                                    .map(table -> table.get("label").toString())
                                    .collect(Collectors.toList());
                            data.put("tables", tableNames);
                        } catch (Exception e) {
                            // 如果获取表列表失败，记录错误但不影响主操作的返回
                            data.put("tablesError", "获取更新后的表列表失败: " + e.getMessage());
                        }
                        break;
                    case "CREATE_INDEX":
                    case "DROP_INDEX":
                        // 对于索引操作，可以尝试获取相关表的索引信息
                        if (tableName != null && !tableName.isEmpty()) {
                            data.put("table", tableName);
                            // 这里可以添加获取表索引的逻辑（需要在QueryService中实现）
                        }
                        break;
                    case "CREATE_VIEW":
                    case "ALTER_VIEW":
                    case "DROP_VIEW":
                        // 对于视图操作，返回更新后的视图列表
                        try {
                            List<Map<String, Object>> views = queryService.getDatabaseViews(id, database, null);
                            List<String> viewNames = views.stream()
                                    .map(view -> view.get("label").toString())
                                    .collect(Collectors.toList());
                            data.put("views", viewNames);
                        } catch (Exception e) {
                            data.put("viewsError", "获取更新后的视图列表失败: " + e.getMessage());
                        }
                        break;
                    case "CREATE_PROCEDURE":
                    case "CREATE_FUNCTION":
                    case "DROP_PROCEDURE":
                    case "DROP_FUNCTION":
                        // 对于存储过程和函数操作，返回更新后的列表
                        try {
                            List<Map<String, Object>> functions = queryService.getDatabaseFunctions(id, database, null);
                            List<String> functionNames = functions.stream()
                                    .map(function -> function.get("label").toString())
                                    .collect(Collectors.toList());
                            data.put("functions", functionNames);
                        } catch (Exception e) {
                            data.put("functionsError", "获取更新后的函数/存储过程列表失败: " + e.getMessage());
                        }
                        break;
                    case "CREATE_TRIGGER":
                    case "DROP_TRIGGER":
                        // 触发器操作可能需要在QueryService中添加获取触发器列表的方法
                        break;
                    case "INSERT":
                    case "UPDATE":
                    case "DELETE":
                        // DML操作已经包含了受影响行数
                        if (tableName != null && !tableName.isEmpty()) {
                            data.put("table", tableName);
                        }
                        break;
                    case "EXPLAIN":
                        // 对于EXPLAIN查询，特殊处理结果格式
                        data.put("explainPlan", true);
                        break;
                    case "TRANSACTION":
                        // 事务控制语句
                        data.put("transactionControl", true);
                        break;
                    default:
                        // 其他类型的SQL
                        break;
                }
                
                result.put("data", data);
            } else {
                result.put("message", queryResponse.getErrorMessage());
                // 添加SQL类型信息，即使执行失败
                result.put("sqlType", sqlType);
            }
            
            return success(result);
        } catch (Exception e) {
            return error("执行查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 判断SQL语句类型
     * @param sql SQL语句（已转换为大写）
     * @return SQL类型
     */
    private String determineSqlType(String sql) {
        sql = sql.replaceAll("\\s+", " ").trim();
        
        if (sql.startsWith("SELECT ") || sql.startsWith("WITH ")) {
            return "SELECT";
        } else if (sql.startsWith("INSERT ")) {
            return "INSERT";
        } else if (sql.startsWith("UPDATE ")) {
            return "UPDATE";
        } else if (sql.startsWith("DELETE ")) {
            return "DELETE";
        } else if (sql.startsWith("CREATE TABLE ") || sql.startsWith("CREATE TEMPORARY TABLE ")) {
            return "CREATE_TABLE";
        } else if (sql.startsWith("ALTER TABLE ")) {
            return "ALTER_TABLE";
        } else if (sql.startsWith("DROP TABLE ")) {
            return "DROP_TABLE";
        } else if (sql.startsWith("CREATE VIEW ")) {
            return "CREATE_VIEW";
        } else if (sql.startsWith("ALTER VIEW ")) {
            return "ALTER_VIEW";
        } else if (sql.startsWith("DROP VIEW ")) {
            return "DROP_VIEW";
        } else if (sql.startsWith("CREATE INDEX ") || sql.startsWith("CREATE UNIQUE INDEX ")) {
            return "CREATE_INDEX";
        } else if (sql.startsWith("DROP INDEX ")) {
            return "DROP_INDEX";
        } else if (sql.startsWith("CREATE PROCEDURE ")) {
            return "CREATE_PROCEDURE";
        } else if (sql.startsWith("CREATE FUNCTION ")) {
            return "CREATE_FUNCTION";
        } else if (sql.startsWith("DROP PROCEDURE ")) {
            return "DROP_PROCEDURE";
        } else if (sql.startsWith("DROP FUNCTION ")) {
            return "DROP_FUNCTION";
        } else if (sql.startsWith("CREATE TRIGGER ")) {
            return "CREATE_TRIGGER";
        } else if (sql.startsWith("DROP TRIGGER ")) {
            return "DROP_TRIGGER";
        } else if (sql.startsWith("BEGIN ") || sql.startsWith("START TRANSACTION") || 
                  sql.startsWith("COMMIT") || sql.startsWith("ROLLBACK")) {
            return "TRANSACTION";
        } else if (sql.startsWith("EXPLAIN ")) {
            return "EXPLAIN";
        } else if (sql.startsWith("DESCRIBE ") || sql.startsWith("DESC ")) {
            return "DESCRIBE";
        } else if (sql.startsWith("SHOW ")) {
            return "SHOW";
        } else if (sql.startsWith("CREATE DATABASE ") || sql.startsWith("CREATE SCHEMA ")) {
            return "CREATE_DATABASE";
        } else if (sql.startsWith("DROP DATABASE ") || sql.startsWith("DROP SCHEMA ")) {
            return "DROP_DATABASE";
        } else if (sql.startsWith("USE ")) {
            return "USE_DATABASE";
        } else if (sql.startsWith("SET ")) {
            return "SET";
        } else if (sql.startsWith("GRANT ")) {
            return "GRANT";
        } else if (sql.startsWith("REVOKE ")) {
            return "REVOKE";
        } else if (sql.startsWith("CREATE USER ")) {
            return "CREATE_USER";
        } else if (sql.startsWith("ALTER USER ")) {
            return "ALTER_USER";
        } else if (sql.startsWith("DROP USER ")) {
            return "DROP_USER";
        } else if (sql.startsWith("CREATE EVENT ")) {
            return "CREATE_EVENT";
        } else if (sql.startsWith("ALTER EVENT ")) {
            return "ALTER_EVENT";
        } else if (sql.startsWith("DROP EVENT ")) {
            return "DROP_EVENT";
        }
        
        // 无法识别的SQL类型
        return "UNKNOWN";
    }

    /**
     * 获取数据库的函数列表
     * @param id 连接ID
     * @param database 数据库名称
     * @return 函数列表
     */
    @GetMapping("/{id}/databases/{database}/functions")
    public ResponseEntity<ApiResponse<List<String>>> getFunctions(
            @PathVariable Long id, 
            @PathVariable String database) {
        try {
            // 获取函数列表
            List<Map<String, Object>> functions = queryService.getDatabaseFunctions(id, database, null);
            
            // 从函数信息中提取函数名
            List<String> functionNames = functions.stream()
                    .map(function -> function.get("label").toString())
                    .collect(Collectors.toList());
            
            return success(functionNames);
        } catch (Exception e) {
            return error("获取函数列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库的事件列表
     * @param id 连接ID
     * @param database 数据库名称
     * @return 事件列表
     */
    @GetMapping("/{id}/databases/{database}/events")
    public ResponseEntity<ApiResponse<List<String>>> getEvents(
            @PathVariable Long id, 
            @PathVariable String database) {
        try {
            // 获取事件列表
            List<Map<String, Object>> events = queryService.getDatabaseEvents(id, database, null);
            
            // 从事件信息中提取事件名
            List<String> eventNames = events.stream()
                    .map(event -> event.get("label").toString())
                    .collect(Collectors.toList());
            
            return success(eventNames);
        } catch (Exception e) {
            return error("获取事件列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询特定表的数据
     * @param id 连接ID
     * @param database 数据库名称
     * @param table 表名
     * @return 表数据
     */
    @GetMapping("/{id}/databases/{database}/tables/{table}/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTableData(
            @PathVariable Long id, 
            @PathVariable String database,
            @PathVariable String table,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset) {
        try {
            // 创建查询SQL
            String sql = String.format("SELECT * FROM %s LIMIT %d OFFSET %d", table, limit, offset);
            
            // 创建QueryRequest对象
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setConnectionId(id);
            queryRequest.setDatabase(database);
            queryRequest.setTableName(table);
            queryRequest.setSql(sql);
            
            // 执行查询并返回结果
            QueryResponse queryResponse = queryService.executeQuery(queryRequest);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", queryResponse.isSuccess());
            
            if (queryResponse.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("columns", queryResponse.getColumns());
                data.put("data", queryResponse.getData());
                data.put("total", queryResponse.getData() != null ? queryResponse.getData().size() : 0);
                data.put("database", database);
                data.put("table", table);
                data.put("limit", limit);
                data.put("offset", offset);
                
                result.put("data", data);
            } else {
                result.put("message", queryResponse.getErrorMessage());
            }
            
            return success(result);
        } catch (Exception e) {
            return error("获取表数据失败: " + e.getMessage());
        }
    }

    /**
     * 新增表数据
     * @param id 连接ID
     * @param database 数据库名称
     * @param table 表名
     * @param request 包含表数据的请求体
     * @return 插入结果
     */
    @PostMapping("/{id}/databases/{database}/tables/{table}/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> insertTableData(
            @PathVariable Long id, 
            @PathVariable String database,
            @PathVariable String table,
            @RequestBody Map<String, Object> request) {
        try {
            // 从请求中获取数据字段
            Map<String, Object> rowData = (Map<String, Object>) request.get("data");
            if (rowData == null || rowData.isEmpty()) {
                return error("数据不能为空");
            }
            
            // 构建插入SQL语句
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO `").append(database).append("`.`").append(table).append("` (");
            
            // 添加字段名
            StringBuilder fieldsBuilder = new StringBuilder();
            StringBuilder valuesBuilder = new StringBuilder();
            
            int i = 0;
            List<Object> values = new ArrayList<>();
            for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                if (i > 0) {
                    fieldsBuilder.append(", ");
                    valuesBuilder.append(", ");
                }
                fieldsBuilder.append("`").append(entry.getKey()).append("`");
                valuesBuilder.append("?");
                values.add(entry.getValue());
                i++;
            }
            
            sqlBuilder.append(fieldsBuilder).append(") VALUES (").append(valuesBuilder).append(")");
            
            // 创建QueryRequest对象
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setConnectionId(id);
            queryRequest.setDatabase(database);
            queryRequest.setTableName(table);
            queryRequest.setSql(sqlBuilder.toString());
            queryRequest.setParameters(values);
            
            // 执行插入操作
            QueryResponse queryResponse = queryService.executeQuery(queryRequest);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", queryResponse.isSuccess());
            
            if (queryResponse.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("affectedRows", queryResponse.getAffectedRows());
                data.put("database", database);
                data.put("table", table);
                data.put("message", "数据插入成功");
                
                result.put("data", data);
            } else {
                result.put("message", queryResponse.getErrorMessage());
            }
            
            return success(result);
        } catch (Exception e) {
            return error("插入数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新表数据
     * @param id 连接ID
     * @param database 数据库名称
     * @param table 表名
     * @param request 包含表数据和条件的请求体
     * @return 更新结果
     */
    @PutMapping("/{id}/databases/{database}/tables/{table}/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTableData(
            @PathVariable Long id, 
            @PathVariable String database,
            @PathVariable String table,
            @RequestBody Map<String, Object> request) {
        try {
            // 从请求中获取数据和条件
            Map<String, Object> updateData = (Map<String, Object>) request.get("data");
            Map<String, Object> condition = (Map<String, Object>) request.get("condition");
            
            if (updateData == null || updateData.isEmpty()) {
                return error("更新数据不能为空");
            }
            
            if (condition == null || condition.isEmpty()) {
                return error("更新条件不能为空");
            }
            
            // 构建更新SQL语句
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("UPDATE `").append(database).append("`.`").append(table).append("` SET ");
            
            // 添加更新字段
            int i = 0;
            List<Object> paramValues = new ArrayList<>();
            for (Map.Entry<String, Object> entry : updateData.entrySet()) {
                if (i > 0) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("`").append(entry.getKey()).append("` = ?");
                paramValues.add(entry.getValue());
                i++;
            }
            
            // 添加WHERE条件
            sqlBuilder.append(" WHERE ");
            i = 0;
            for (Map.Entry<String, Object> entry : condition.entrySet()) {
                if (i > 0) {
                    sqlBuilder.append(" AND ");
                }
                sqlBuilder.append("`").append(entry.getKey()).append("` = ?");
                paramValues.add(entry.getValue());
                i++;
            }
            
            // 创建QueryRequest对象
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setConnectionId(id);
            queryRequest.setDatabase(database);
            queryRequest.setTableName(table);
            queryRequest.setSql(sqlBuilder.toString());
            queryRequest.setParameters(paramValues);
            
            // 执行更新操作
            QueryResponse queryResponse = queryService.executeQuery(queryRequest);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", queryResponse.isSuccess());
            
            if (queryResponse.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("affectedRows", queryResponse.getAffectedRows());
                data.put("database", database);
                data.put("table", table);
                data.put("message", "数据更新成功");
                
                result.put("data", data);
            } else {
                result.put("message", queryResponse.getErrorMessage());
            }
            
            return success(result);
        } catch (Exception e) {
            return error("更新数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除表数据
     * @param id 连接ID
     * @param database 数据库名称
     * @param table 表名
     * @param request 包含删除条件的请求体
     * @return 删除结果
     */
    @DeleteMapping("/{id}/databases/{database}/tables/{table}/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTableData(
            @PathVariable Long id, 
            @PathVariable String database,
            @PathVariable String table,
            @RequestBody Map<String, Object> request) {
        try {
            // 从请求中获取删除条件
            Map<String, Object> condition = (Map<String, Object>) request.get("condition");
            
            if (condition == null || condition.isEmpty()) {
                return error("删除条件不能为空");
            }
            
            // 构建删除SQL语句
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("DELETE FROM `").append(database).append("`.`").append(table).append("` WHERE ");
            
            // 添加条件
            int i = 0;
            List<Object> paramValues = new ArrayList<>();
            for (Map.Entry<String, Object> entry : condition.entrySet()) {
                if (i > 0) {
                    sqlBuilder.append(" AND ");
                }
                sqlBuilder.append("`").append(entry.getKey()).append("` = ?");
                paramValues.add(entry.getValue());
                i++;
            }
            
            // 创建QueryRequest对象
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setConnectionId(id);
            queryRequest.setDatabase(database);
            queryRequest.setTableName(table);
            queryRequest.setSql(sqlBuilder.toString());
            queryRequest.setParameters(paramValues);
            
            // 执行删除操作
            QueryResponse queryResponse = queryService.executeQuery(queryRequest);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", queryResponse.isSuccess());
            
            if (queryResponse.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("affectedRows", queryResponse.getAffectedRows());
                data.put("database", database);
                data.put("table", table);
                data.put("message", "数据删除成功");
                
                result.put("data", data);
            } else {
                result.put("message", queryResponse.getErrorMessage());
            }
            
            return success(result);
        } catch (Exception e) {
            return error("删除数据失败: " + e.getMessage());
        }
    }
} 
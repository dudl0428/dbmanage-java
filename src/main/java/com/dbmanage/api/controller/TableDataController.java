package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.service.QueryService;
import com.dbmanage.api.dto.query.QueryRequest;
import com.dbmanage.api.dto.query.QueryResponse;
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

/**
 * 表数据操作控制器
 * 处理表数据的增删改查操作
 */
@RestController
@RequestMapping("/table-data")
public class TableDataController {
    
    private static final Logger logger = LoggerFactory.getLogger(TableDataController.class);

    @Autowired
    private QueryService queryService;

    /**
     * 获取表结构信息
     * @param connectionId 连接ID
     * @param database 数据库名
     * @param table 表名
     * @return 表结构信息
     */
    @GetMapping("/{connectionId}/{database}/{table}/structure")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableStructure(
            @PathVariable Long connectionId,
            @PathVariable String database,
            @PathVariable String table) {
        try {
            logger.info("获取表 {}.{} 的结构信息", database, table);
            
            // 构建完整的表名（包含数据库名）
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
     * 获取表数据
     * @param connectionId 连接ID
     * @param database 数据库名
     * @param table 表名
     * @param page 页码（从1开始）
     * @param pageSize 每页记录数
     * @return 表数据和分页信息
     */
    @GetMapping("/{connectionId}/{database}/{table}/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTableData(
            @PathVariable Long connectionId,
            @PathVariable String database,
            @PathVariable String table,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize) {
        try {
            logger.info("获取表 {}.{} 的数据，页码：{}，每页记录数：{}", database, table, page, pageSize);
            
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 构建查询SQL
            String countSql = String.format("SELECT COUNT(*) as total FROM `%s`.`%s`", database, table);
            String dataSql = String.format("SELECT * FROM `%s`.`%s` LIMIT %d, %d", database, table, offset, pageSize);
            
            // 创建查询请求
            QueryRequest countRequest = new QueryRequest();
            countRequest.setConnectionId(connectionId);
            countRequest.setDatabase(database);
            countRequest.setSql(countSql);
            
            QueryRequest dataRequest = new QueryRequest();
            dataRequest.setConnectionId(connectionId);
            dataRequest.setDatabase(database);
            dataRequest.setSql(dataSql);
            
            // 执行查询
            QueryResponse countResponse = queryService.executeQuery(countRequest);
            QueryResponse dataResponse = queryService.executeQuery(dataRequest);
            
            // 处理结果
            Map<String, Object> result = new HashMap<>();
            
            // 获取总记录数
            long total = 0;
            if (countResponse.isSuccess() && countResponse.getData() != null && !countResponse.getData().isEmpty()) {
                total = ((Number) countResponse.getData().get(0).get("total")).longValue();
            }
            
            // 设置分页信息
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", total);
            pagination.put("current", page);
            pagination.put("pageSize", pageSize);
            pagination.put("totalPages", (total + pageSize - 1) / pageSize);
            
            // 设置返回结果
            result.put("pagination", pagination);
            result.put("columns", dataResponse.getColumns());
            result.put("data", dataResponse.getData());
            
            logger.info("成功获取表数据，总记录数：{}，当前页数据量：{}", 
                total, dataResponse.getData() != null ? dataResponse.getData().size() : 0);
            
            return ResponseEntity.ok(ApiResponse.success("获取表数据成功", result));
            
        } catch (Exception e) {
            logger.error("获取表数据失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "获取表数据失败: " + e.getMessage()));
        }
    }

    /**
     * 插入表数据
     * @param connectionId 连接ID
     * @param database 数据库名
     * @param table 表名
     * @param data 要插入的数据
     * @return 插入结果
     */
    @PostMapping("/{connectionId}/{database}/{table}/insert")
    public ResponseEntity<ApiResponse<Map<String, Object>>> insertTableData(
            @PathVariable Long connectionId,
            @PathVariable String database,
            @PathVariable String table,
            @RequestBody Map<String, Object> data) {
        try {
            logger.info("开始插入数据到表 {}.{}", database, table);
            logger.debug("插入数据: {}", data);

            // 构建INSERT语句
            StringBuilder sql = new StringBuilder();
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            
            sql.append("INSERT INTO `").append(database).append("`.`").append(table).append("` (");
            
            boolean first = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!first) {
                    columns.append(", ");
                    values.append(", ");
                }
                columns.append("`").append(entry.getKey()).append("`");
                values.append("?");
                first = false;
            }
            
            sql.append(columns).append(") VALUES (").append(values).append(")");
            
            // 执行插入操作
            Map<String, Object> result = queryService.executeUpdate(
                connectionId,
                sql.toString(),
                data.values().toArray()
            );
            
            logger.info("数据插入成功");
            return ResponseEntity.ok(ApiResponse.success("数据插入成功", result));
            
        } catch (Exception e) {
            logger.error("插入数据失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "插入数据失败: " + e.getMessage()));
        }
    }

    /**
     * 更新表数据
     * @param connectionId 连接ID
     * @param database 数据库名
     * @param table 表名
     * @param data 要更新的数据
     * @param condition 更新条件
     * @return 更新结果
     */
    @PostMapping("/{connectionId}/{database}/{table}/update")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTableData(
            @PathVariable Long connectionId,
            @PathVariable String database,
            @PathVariable String table,
            @RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            Map<String, Object> condition = (Map<String, Object>) request.get("condition");
            
            if (data == null || data.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "更新数据不能为空"));
            }
            
            if (condition == null || condition.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "更新条件不能为空"));
            }

            logger.info("开始更新表 {}.{} 的数据", database, table);
            logger.debug("更新数据: {}, 条件: {}", data, condition);

            // 构建UPDATE语句
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE `").append(database).append("`.`").append(table).append("` SET ");
            
            // 设置更新的字段
            boolean first = true;
            for (String key : data.keySet()) {
                if (!first) {
                    sql.append(", ");
                }
                sql.append("`").append(key).append("` = ?");
                first = false;
            }
            
            // 添加WHERE条件
            sql.append(" WHERE ");
            first = true;
            for (String key : condition.keySet()) {
                if (!first) {
                    sql.append(" AND ");
                }
                sql.append("`").append(key).append("` = ?");
                first = false;
            }
            
            // 合并参数
            Object[] params = new Object[data.size() + condition.size()];
            int i = 0;
            for (Object value : data.values()) {
                params[i++] = value;
            }
            for (Object value : condition.values()) {
                params[i++] = value;
            }
            
            // 执行更新操作
            Map<String, Object> result = queryService.executeUpdate(
                connectionId,
                sql.toString(),
                params
            );
            
            logger.info("数据更新成功");
            return ResponseEntity.ok(ApiResponse.success("数据更新成功", result));
            
        } catch (Exception e) {
            logger.error("更新数据失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "更新数据失败: " + e.getMessage()));
        }
    }

    /**
     * 删除表数据
     *
     * @param connectionId 连接ID
     * @param database     数据库名
     * @param table        表名
     * @param requestBody  请求体包含删除条件
     * @return 删除结果
     */
    @PostMapping("/{connectionId}/{database}/{table}/delete")
    public ResponseEntity<Map<String, Object>> deleteTableData(
            @PathVariable Integer connectionId,
            @PathVariable String database,
            @PathVariable String table,
            @RequestBody Map<String, Object> requestBody) {
        
        logger.info("删除表数据 - 连接ID: {}, 数据库: {}, 表: {}", connectionId, database, table);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取删除条件
            Map<String, Object> condition = (Map<String, Object>) requestBody.get("condition");
            
            if (condition == null || condition.isEmpty()) {
                logger.error("删除表数据失败 - 没有提供删除条件");
                response.put("success", false);
                response.put("message", "删除失败：必须提供删除条件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 构建WHERE子句
            StringBuilder whereSql = new StringBuilder();
            List<Object> params = new ArrayList<>();
            
            int index = 0;
            for (Map.Entry<String, Object> entry : condition.entrySet()) {
                if (index > 0) {
                    whereSql.append(" AND ");
                }
                
                String columnName = entry.getKey();
                Object value = entry.getValue();
                
                if (value == null) {
                    whereSql.append("`").append(columnName).append("` IS NULL");
                } else {
                    whereSql.append("`").append(columnName).append("` = ?");
                    params.add(value);
                }
                
                index++;
            }
            
            // 构建完整DELETE语句
            String sql = "DELETE FROM `" + database + "`.`" + table + "` WHERE " + whereSql.toString();
            
            logger.info("执行删除SQL: {}", sql);
            
            // 执行删除操作
            long startTime = System.currentTimeMillis();
            Map<String, Object> updateResult = queryService.executeUpdate(connectionId.longValue(), sql, params.toArray());
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 检查执行结果
            if (updateResult.containsKey("success") && (boolean) updateResult.get("success")) {
                response.put("success", true);
                response.put("message", "删除成功");
                response.put("affectedRows", updateResult.getOrDefault("affectedRows", 0));
                response.put("executionTime", executionTime);
            } else {
                response.put("success", false);
                response.put("message", "删除失败: " + updateResult.getOrDefault("errorMessage", "未知错误"));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("删除表数据异常", e);
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 
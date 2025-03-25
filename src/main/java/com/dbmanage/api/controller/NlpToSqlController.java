package com.dbmanage.api.controller;

import com.dbmanage.api.service.NlpToSqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自然语言转SQL接口
 * 支持OpenAI和DeepSeek大语言模型
 */
@RestController
@RequestMapping("/ai")
public class NlpToSqlController {

    @Autowired
    private  NlpToSqlService nlpToSqlService;

    /**
     * 将自然语言转换为SQL
     * 
     * 请求参数示例:
     * {
     *   "query": "查询用户信息里面年龄大于18的数据",
     *   "model": "openai", // 或 "deepseek"
     *   "connectionType": "mysql", // 数据库类型，如mysql, postgresql等
     *   "database": "jesite", // 数据库名称
     *   "schema": { // 完整的数据库结构信息
     *     "databaseType": "mysql",
     *     "database": "jesite",
     *     "tables": [
     *       {
     *         "name": "js_user", // 表名
     *         "comment": "用户信息表", // 表注释，帮助AI理解表的用途
     *         "columns": [
     *           {
     *             "name": "id", // 字段名
     *             "type": "bigint", // 字段类型
     *             "comment": "用户ID", // 字段注释，帮助AI理解字段含义
     *             "nullable": false,
     *             "key": "PRI"
     *           },
     *           {
     *             "name": "username",
     *             "type": "varchar(100)",
     *             "comment": "用户名",
     *             "nullable": false
     *           },
     *           {
     *             "name": "age",
     *             "type": "int",
     *             "comment": "用户年龄",
     *             "nullable": true
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * }
     *
     * @param requestBody 包含查询和上下文信息的请求体
     * @return 生成的SQL响应
     */
    @PostMapping("/generate-sql")
    public ResponseEntity<Map<String, Object>> generateSql(@RequestBody Map<String, Object> requestBody) {
        try {
            // 从请求体中提取参数
            String query = (String) requestBody.get("query");
            String model = (String) requestBody.getOrDefault("model", "openai");
            String connectionType = (String) requestBody.getOrDefault("connectionType", "mysql");
            String database = (String) requestBody.getOrDefault("database", "");
            Object schemaObj = requestBody.get("schema");
            String schema = "";
            
            // 参数验证
            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "查询描述不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 处理schema参数
            if (schemaObj != null) {
                // 如果是字符串，直接使用
                if (schemaObj instanceof String) {
                    schema = (String) schemaObj;
                } 
                // 如果是Map或其他复杂对象，转换为JSON字符串
                else {
                    try {
                        // 使用Jackson等JSON库进行转换
                        schema = schemaObj.toString();
                    } catch (Exception e) {
                        schema = "";
                    }
                }
            }
            
            // 如果schema为空，返回警告
            if (schema == null || schema.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "缺少数据库结构信息(schema)，这会导致AI无法理解数据库结构，生成的SQL可能不准确");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 调用服务生成SQL
            String generatedSql = nlpToSqlService.generateSql(query, connectionType, schema, model);
            
            // 构建成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", generatedSql);
            response.put("query", query);
            response.put("model", model);
            response.put("connectionType", connectionType);
            response.put("database", database);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 构建错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 获取SQL自动补全建议
     *
     * @param partialSql 部分SQL
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return 补全建议列表
     */
    @PostMapping("/sql-completions")
    public ResponseEntity<Map<String, Object>> getSqlCompletions(
            @RequestParam String partialSql,
            @RequestParam(required = false, defaultValue = "sql") String dialect,
            @RequestParam(required = false) String schemaInfo,
            @RequestParam(required = false, defaultValue = "deepseek") String modelType) {
        
        try {
            List<String> completions = nlpToSqlService.getSqlCompletions(partialSql, dialect, schemaInfo, modelType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("partialSql", partialSql);
            response.put("completions", completions);
            response.put("modelType", modelType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取SQL查询示例
     *
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return 示例列表
     */
    @GetMapping("/sql-examples")
    public ResponseEntity<Map<String, Object>> getSqlExamples(
            @RequestParam(required = false, defaultValue = "sql") String dialect,
            @RequestParam(required = false) String schemaInfo,
            @RequestParam(required = false, defaultValue = "deepseek") String modelType) {
        
        try {
            List<Map<String, String>> examples = nlpToSqlService.getSqlExamples(dialect, schemaInfo, modelType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("examples", examples);
            response.put("modelType", modelType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }
} 
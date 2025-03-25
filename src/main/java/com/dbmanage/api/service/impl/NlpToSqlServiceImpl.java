package com.dbmanage.api.service.impl;

import com.dbmanage.api.service.DeepSeekService;
import com.dbmanage.api.service.NlpToSqlService;
import com.dbmanage.api.service.SqlFormatterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 增强型NLP到SQL服务实现类
 * 改进了数据库结构格式化逻辑，更好地处理表和字段注释
 */
@Service
public class NlpToSqlServiceImpl extends NlpToSqlService {

    private static final Logger logger = LoggerFactory.getLogger(NlpToSqlServiceImpl.class);
    
    /**
     * 构造函数
     */
    public NlpToSqlServiceImpl(
            @Value("${openai.api-key:}") String openaiApiKey,
            @Value("${openai.model:gpt-3.5-turbo}") String openaiModel,
            @Value("${openai.timeout-seconds:30}") int openaiTimeoutSeconds,
            @Value("${openai.base-url:https://api.gptsapi.net/v1/}") String openaiBaseUrl,
            @Value("${deepseek.api-key:}") String deepseekApiKey,
            @Value("${deepseek.model:deepseek-chat}") String deepseekModel,
            @Value("${deepseek.timeout-seconds:30}") int deepseekTimeoutSeconds,
            SqlFormatterService sqlFormatterService) {
        super(openaiApiKey, openaiModel, openaiTimeoutSeconds, openaiBaseUrl, deepseekApiKey, deepseekModel, deepseekTimeoutSeconds, sqlFormatterService);
    }

    /**
     * 重载生成SQL的方法，使用增强的结构格式化
     * 
     * @param naturalLanguageQuery 自然语言查询
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @param modelType 模型类型
     * @return 生成的SQL
     */
    @Override
    public String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo, String modelType) {
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            // 使用增强的格式化方法处理schema信息
            schemaInfo = enhancedFormatSchemaInfo(schemaInfo);
        }
        return super.generateSql(naturalLanguageQuery, dialect, schemaInfo, modelType);
    }

    /**
     * 增强版数据库结构格式化方法，提供更好的表和字段注释处理
     * 
     * @param schemaInfo 原始的数据库结构信息JSON字符串
     * @return 格式化后的文本
     */
    private String enhancedFormatSchemaInfo(String schemaInfo) {
        try {
            // 尝试将JSON字符串转换为Map对象进行处理
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> schemaMap;
            
            try {
                // 尝试解析JSON
                schemaMap = mapper.readValue(schemaInfo, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                logger.warn("无法解析schema JSON，尝试使用字符串处理: {}", e.getMessage());
                return enhancedFormatSchemaInfoWithStringProcessing(schemaInfo);
            }
            
            StringBuilder sb = new StringBuilder();
            
            // 添加数据库名称和类型信息
            String databaseType = (String) schemaMap.getOrDefault("databaseType", "未知类型");
            String database = (String) schemaMap.getOrDefault("database", "未知数据库");
            
            sb.append("数据库信息:\n");
            sb.append("- 数据库名称: ").append(database).append("\n");
            sb.append("- 数据库类型: ").append(databaseType).append("\n\n");
            
            // 处理表信息
            List<Map<String, Object>> tables = (List<Map<String, Object>>) schemaMap.get("tables");
            if (tables != null && !tables.isEmpty()) {
                sb.append("数据库包含以下表:\n\n");
                
                // 首先收集所有表的主键信息，用于后续分析表间关系
                Map<String, List<String>> tablePrimaryKeys = new HashMap<>();
                Map<String, String> tableComments = new HashMap<>();
                
                for (Map<String, Object> table : tables) {
                    String tableName = (String) table.get("name");
                    List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
                    List<String> primaryKeys = new ArrayList<>();
                    
                    if (columns != null) {
                        for (Map<String, Object> column : columns) {
                            Object isPrimaryKeyObj = column.getOrDefault("isPrimaryKey", false);
                            boolean isPrimaryKey = false;
                            
                            if (isPrimaryKeyObj instanceof Boolean) {
                                isPrimaryKey = (Boolean) isPrimaryKeyObj;
                            } else if (isPrimaryKeyObj instanceof String) {
                                isPrimaryKey = "true".equalsIgnoreCase((String) isPrimaryKeyObj) || 
                                              "PRI".equalsIgnoreCase((String) isPrimaryKeyObj);
                            }
                            
                            if (isPrimaryKey) {
                                primaryKeys.add((String) column.get("name"));
                            }
                        }
                    }
                    
                    tablePrimaryKeys.put(tableName, primaryKeys);
                    tableComments.put(tableName, (String) table.getOrDefault("comment", ""));
                }
                
                // 然后详细输出每个表的信息
                for (Map<String, Object> table : tables) {
                    String tableName = (String) table.get("name");
                    String tableComment = (String) table.getOrDefault("comment", "");
                    
                    // 添加表名和注释
                    sb.append("表英文名: ").append(tableName);
                    if (tableComment != null && !tableComment.trim().isEmpty()) {
                        sb.append("\n表中文名: ").append(tableComment);
                    }
                    sb.append("\n");
                    
                    // 添加主键信息
                    List<String> primaryKeys = tablePrimaryKeys.get(tableName);
                    if (primaryKeys != null && !primaryKeys.isEmpty()) {
                        sb.append("主键: ").append(String.join(", ", primaryKeys)).append("\n");
                    }
                    
                    // 识别可能的外键关系
                    List<String> possibleRelations = new ArrayList<>();
                    List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
                    
                    if (columns != null) {
                        for (Map<String, Object> column : columns) {
                            String columnName = (String) column.get("name");
                            
                            // 检测常见的外键命名模式
                            if (columnName.toLowerCase().endsWith("_id") || columnName.toLowerCase().startsWith("id_") || 
                                columnName.equalsIgnoreCase("parent_id") || columnName.equalsIgnoreCase("parent")) {
                                
                                // 尝试从列名中提取可能的关联表名
                                String possibleTableName = null;
                                if (columnName.toLowerCase().endsWith("_id")) {
                                    possibleTableName = columnName.substring(0, columnName.length() - 3);
                                } else if (columnName.toLowerCase().startsWith("id_")) {
                                    possibleTableName = columnName.substring(3);
                                }
                                
                                // 如果找到可能的表名，检查它是否存在
                                if (possibleTableName != null && tablePrimaryKeys.containsKey(possibleTableName)) {
                                    String relatedTableComment = tableComments.get(possibleTableName);
                                    possibleRelations.add(columnName + " -> " + possibleTableName + 
                                                         (relatedTableComment != null && !relatedTableComment.isEmpty() ? 
                                                          " (" + relatedTableComment + ")" : ""));
                                }
                            }
                        }
                    }
                    
                    // 添加可能的表关系信息
                    if (!possibleRelations.isEmpty()) {
                        sb.append("可能的表关系:\n");
                        for (String relation : possibleRelations) {
                            sb.append("  - ").append(relation).append("\n");
                        }
                    }
                    
                    // 添加表的列信息
                    if (columns != null && !columns.isEmpty()) {
                        sb.append("列信息:\n");
                        
                        for (Map<String, Object> column : columns) {
                            String columnName = (String) column.get("name");
                            String columnType = (String) column.get("type");
                            String columnComment = (String) column.getOrDefault("comment", "");
                            Object nullableObj = column.get("nullable");
                            Object isPrimaryKeyObj = column.getOrDefault("isPrimaryKey", false);
                            
                            // 处理nullable字段可能是Boolean或String类型的情况
                            boolean nullable = true;
                            if (nullableObj instanceof Boolean) {
                                nullable = (Boolean) nullableObj;
                            } else if (nullableObj instanceof String) {
                                nullable = "true".equalsIgnoreCase((String) nullableObj) || 
                                           "yes".equalsIgnoreCase((String) nullableObj);
                            }
                            
                            // 处理isPrimaryKey字段可能是Boolean或String类型的情况
                            boolean isPrimaryKey = false;
                            if (isPrimaryKeyObj instanceof Boolean) {
                                isPrimaryKey = (Boolean) isPrimaryKeyObj;
                            } else if (isPrimaryKeyObj instanceof String) {
                                isPrimaryKey = "true".equalsIgnoreCase((String) isPrimaryKeyObj) || 
                                               "PRI".equalsIgnoreCase((String) isPrimaryKeyObj);
                            }
                            
                            // 构建字段信息
                            sb.append("  - ").append(columnName).append(" (类型: ").append(columnType);
                            
                            // 添加主键、非空等约束信息
                            if (isPrimaryKey) {
                                sb.append(", 主键");
                            }
                            if (!nullable) {
                                sb.append(", NOT NULL");
                            }
                            
                            // 添加列注释
                            if (columnComment != null && !columnComment.trim().isEmpty()) {
                                sb.append(", 说明: ").append(columnComment);
                            }
                            
                            sb.append(")\n");
                        }
                    }
                    
                    // 添加分隔符
                    sb.append("\n");
                }
                
                // 添加表间关系的概览
                sb.append("表间关系概览:\n");
                for (Map<String, Object> table : tables) {
                    String tableName = (String) table.get("name");
                    List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
                    
                    if (columns != null) {
                        for (Map<String, Object> column : columns) {
                            String columnName = (String) column.get("name");
                            
                            // 检测常见的外键命名模式
                            if (columnName.toLowerCase().endsWith("_id") || columnName.toLowerCase().endsWith("id")) {
                                String baseTableName = tableName;
                                String baseTableComment = tableComments.get(tableName);
                                
                                // 尝试从列名中提取可能的关联表名
                                String possibleTableName = null;
                                if (columnName.toLowerCase().endsWith("_id")) {
                                    possibleTableName = columnName.substring(0, columnName.length() - 3);
                                }
                                
                                // 如果找到可能的表名，检查它是否存在
                                if (possibleTableName != null && tablePrimaryKeys.containsKey(possibleTableName)) {
                                    String relatedTableComment = tableComments.get(possibleTableName);
                                    sb.append("  - ").append(baseTableName);
                                    if (baseTableComment != null && !baseTableComment.isEmpty()) {
                                        sb.append(" (").append(baseTableComment).append(")");
                                    }
                                    sb.append(" 通过字段 ").append(columnName).append(" 关联到 ")
                                      .append(possibleTableName);
                                    if (relatedTableComment != null && !relatedTableComment.isEmpty()) {
                                        sb.append(" (").append(relatedTableComment).append(")");
                                    }
                                    sb.append("\n");
                                }
                            }
                        }
                    }
                }
            } else {
                sb.append("未找到表结构信息\n");
            }
            
            return sb.toString();
        } catch (Exception e) {
            logger.error("格式化数据库结构失败: {}", e.getMessage(), e);
            // 如果使用Jackson处理失败，回退到字符串处理方法
            return enhancedFormatSchemaInfoWithStringProcessing(schemaInfo);
        }
    }
    
    /**
     * 使用字符串处理的方式格式化数据库结构信息（兼容旧格式）
     */
    private String enhancedFormatSchemaInfoWithStringProcessing(String schemaInfo) {
        try {
            StringBuilder builder = new StringBuilder();
            
            // 1. 数据库基本信息
            if (schemaInfo.contains("databaseType")) {
                String dbType = extractValue(schemaInfo, "databaseType");
                String dbName = extractValue(schemaInfo, "database");
                builder.append("数据库类型: ").append(dbType).append("\n");
                builder.append("数据库名称: ").append(dbName).append("\n\n");
            }
            
            // 2. 表信息
            builder.append("数据库包含以下表：\n\n");
            
            // 使用简单的方法提取tables数组
            int tablesStart = schemaInfo.indexOf("\"tables\"");
            if (tablesStart > 0) {
                // 表信息处理
                String[] tableParts = schemaInfo.substring(tablesStart).split("\\{\"name\":");
                
                for (int i = 1; i < tableParts.length; i++) {
                    String tablePart = tableParts[i];
                    String tableName = extractValue(tablePart, "name");
                    String tableComment = extractValue(tablePart, "comment");
                    
                    builder.append("表英文名: ").append(tableName);
                    if (tableComment != null && !tableComment.isEmpty()) {
                        builder.append("\n表中文名: ").append(tableComment);
                    }
                    builder.append("\n");
                    
                    // 提取字段信息
                    builder.append("字段列表:\n");
                    
                    int columnsStart = tablePart.indexOf("\"columns\"");
                    if (columnsStart > 0) {
                        String columnsJson = tablePart.substring(columnsStart);
                        // 简单提取字段信息，实际情况可能更复杂
                        String[] columnParts = columnsJson.split("\\{");
                        for (int j = 1; j < columnParts.length; j++) {
                            if (columnParts[j].contains("name")) {
                                String columnName = extractValue(columnParts[j], "name");
                                String columnType = extractValue(columnParts[j], "type");
                                String columnComment = extractValue(columnParts[j], "comment");
                                String nullable = extractValue(columnParts[j], "nullable");
                                String isPrimary = extractValue(columnParts[j], "isPrimaryKey");
                                
                                builder.append("  - ").append(columnName).append(" (类型: ").append(columnType);
                                
                                if ("true".equalsIgnoreCase(isPrimary) || "PRI".equalsIgnoreCase(isPrimary)) {
                                    builder.append(", 主键");
                                }
                                
                                if ("false".equalsIgnoreCase(nullable)) {
                                    builder.append(", NOT NULL");
                                }
                                
                                if (columnComment != null && !columnComment.isEmpty()) {
                                    builder.append(", 说明: ").append(columnComment);
                                }
                                
                                builder.append(")\n");
                            }
                        }
                    }
                    
                    builder.append("\n");
                }
            }
            
            return builder.toString();
        } catch (Exception e) {
            // 处理异常时返回原始信息
            logger.error("格式化数据库结构失败(字符串处理): {}", e.getMessage(), e);
            return schemaInfo;
        }
    }
    
    /**
     * 从JSON片段中提取键值对
     */
    private String extractValue(String json, String key) {
        int keyStart = json.indexOf("\"" + key + "\"");
        if (keyStart < 0) return "";
        
        int valueStart = json.indexOf(":", keyStart) + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return "";
        
        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // 字符串值
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd > valueStart) {
                return json.substring(valueStart + 1, valueEnd);
            }
        } else if (firstChar == 't' || firstChar == 'f' || Character.isDigit(firstChar) || firstChar == '-') {
            // 布尔值或数字
            int valueEnd = json.indexOf(",", valueStart);
            if (valueEnd < 0) valueEnd = json.indexOf("}", valueStart);
            if (valueEnd > valueStart) {
                return json.substring(valueStart, valueEnd).trim();
            }
        }
        
        return "";
    }
}
 
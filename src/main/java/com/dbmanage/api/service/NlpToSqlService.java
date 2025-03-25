package com.dbmanage.api.service;

import java.util.List;
import java.util.Map;

/**
 * AI模型服务接口
 * 处理自然语言转换为SQL
 * 支持OpenAI和DeepSeek模型
 */
public interface NlpToSqlService {

    /**
     * 将自然语言查询转换为SQL
     *
     * @param naturalLanguageQuery 自然语言查询
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息（表和列）
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return 生成的SQL
     */
    String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo, String modelType);

    /**
     * 将自然语言查询转换为SQL，使用默认模型类型
     *
     * @param naturalLanguageQuery 自然语言查询
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息（表和列）
     * @return 生成的SQL
     */
    String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo);
    
    /**
     * 获取SQL自动补全建议
     *
     * @param partialSql 部分SQL查询
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return SQL补全建议列表
     */
    List<String> getSqlCompletions(String partialSql, String dialect, String schemaInfo, String modelType);
    
    /**
     * 获取SQL自动补全建议，使用默认模型类型
     *
     * @param partialSql 部分SQL查询
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @return SQL补全建议列表
     */
    List<String> getSqlCompletions(String partialSql, String dialect, String schemaInfo);
    
    /**
     * 获取SQL示例
     *
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return SQL示例列表，每个示例包含描述和SQL
     */
    List<Map<String, String>> getSqlExamples(String dialect, String schemaInfo, String modelType);
    
    /**
     * 获取SQL示例，使用默认模型类型
     *
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @return SQL示例列表，每个示例包含描述和SQL
     */
    List<Map<String, String>> getSqlExamples(String dialect, String schemaInfo);
} 
package com.dbmanage.api.service.impl;

import com.dbmanage.api.common.Constants;
import com.dbmanage.api.config.AiProperties;
import com.dbmanage.api.config.OpenAiProperties;
import com.dbmanage.api.config.DeepSeekProperties;
import com.dbmanage.api.service.DeepSeekService;
import com.dbmanage.api.service.NlpToSqlService;
import com.dbmanage.api.service.SqlFormatterService;
import com.dbmanage.api.util.MessageResolver;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI模型服务实现类
 * 处理自然语言转换为SQL
 * 支持OpenAI和DeepSeek模型
 */
@Service
public class NlpToSqlServiceImpl implements NlpToSqlService {

    private static final Logger logger = LoggerFactory.getLogger(NlpToSqlServiceImpl.class);
    
    @Autowired
    private OpenAiProperties openAiProperties;
    
    @Autowired
    private DeepSeekProperties deepSeekProperties;
    
    @Autowired
    private AiProperties aiProperties;
    
    @Autowired
    private DeepSeekService deepseekService;
    
    @Autowired
    private SqlFormatterService sqlFormatterService;
    
    private OpenAiService openAiService;
    
    @PostConstruct
    public void init() {
        // 初始化OpenAI服务
        this.openAiService = new OpenAiService(
            openAiProperties.getApiKey(), 
            Duration.ofSeconds(openAiProperties.getTimeoutSeconds())
        );
        
        logger.info(
            MessageResolver.format(Constants.LogMessages.INIT_SERVICE, 
            openAiProperties.getModel(), 
            deepSeekProperties.getModel())
        );
    }

    @Override
    public String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo, String modelType) {
        // 准备聊天消息
        List<ChatMessage> messages = new ArrayList<>();

        // 系统消息，告诉AI它的角色和任务
        messages.add(new ChatMessage(
            Constants.AiModel.SYSTEM_ROLE, 
            aiProperties.getSystemPrompts().get("sql-assistant")
        ));

        // 添加数据库结构信息，格式化结构以便AI更好理解
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            messages.add(new ChatMessage(
                Constants.AiModel.SYSTEM_ROLE,
                MessageResolver.format(Constants.PromptTemplates.SCHEMA_INFO_TEMPLATE, formatSchemaInfo(schemaInfo))
            ));
        }

        // 用户查询，提供明确的任务说明
        String sqlDialect = dialect != null ? dialect : Constants.Sql.DEFAULT_DIALECT;
        messages.add(new ChatMessage(
            Constants.AiModel.USER_ROLE,
            MessageResolver.format(Constants.PromptTemplates.NL_TO_SQL_TEMPLATE, sqlDialect, naturalLanguageQuery)
        ));

        // 创建请求对象
        String modelName = Constants.AiModel.DEEPSEEK.equalsIgnoreCase(modelType) 
            ? deepSeekProperties.getModel() 
            : openAiProperties.getModel();
            
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(messages)
                .temperature(Constants.AiModel.DEFAULT_TEMPERATURE)
                .maxTokens(Constants.AiModel.DEFAULT_MAX_TOKENS)
                .build();

        try {
            String sqlResponse;

            // 根据模型类型选择服务
            if (Constants.AiModel.DEEPSEEK.equalsIgnoreCase(modelType)) {
                sqlResponse = deepseekService.createChatCompletion(completionRequest);
            } else {
                ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
                sqlResponse = result.getChoices().get(0).getMessage().getContent();
            }

            // 去除可能的```sql和```标记
            sqlResponse = cleanupResponse(sqlResponse);

            // 使用SQL格式化工具格式化SQL
            return sqlFormatterService.formatSql(sqlResponse, 
                dialect != null && !dialect.isEmpty() ? dialect : Constants.Sql.DEFAULT_DIALECT);
                
        } catch (Exception e) {
            throw new RuntimeException(MessageResolver.format(Constants.ErrorMessages.GENERATE_SQL_ERROR, e.getMessage()), e);
        }
    }

    @Override
    public String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo) {
        // 默认使用openai作为模型类型
        return generateSql(naturalLanguageQuery, dialect, schemaInfo, Constants.AiModel.OPENAI);
    }
    
    /**
     * 清理API响应，移除代码块标记
     */
    private String cleanupResponse(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        // 去除Markdown代码块
        Pattern pattern = Pattern.compile(Constants.AiModel.CODE_BLOCK_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return input.trim();
    }
    
    /**
     * 格式化数据库结构信息，使其更易于AI理解
     */
    private String formatSchemaInfo(String schemaInfo) {
        // 简单实现，实际应根据schemaInfo结构进行解析和格式化
        return schemaInfo;
    }

    @Override
    public List<String> getSqlCompletions(String partialSql, String dialect, String schemaInfo, String modelType) {
        // 准备聊天消息
        List<ChatMessage> messages = new ArrayList<>();

        // 系统消息，告诉AI它的角色和任务
        messages.add(new ChatMessage(
            Constants.AiModel.SYSTEM_ROLE, 
            aiProperties.getSystemPrompts().get("sql-assistant")
        ));

        // 添加数据库结构信息，格式化结构以便AI更好理解
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            messages.add(new ChatMessage(
                Constants.AiModel.SYSTEM_ROLE,
                MessageResolver.format(Constants.PromptTemplates.SCHEMA_INFO_TEMPLATE, formatSchemaInfo(schemaInfo))
            ));
        }

        // 用户查询，提供明确的任务说明
        String sqlDialect = dialect != null ? dialect : Constants.Sql.DEFAULT_DIALECT;
        messages.add(new ChatMessage(
            Constants.AiModel.USER_ROLE,
            MessageResolver.format(Constants.PromptTemplates.SQL_COMPLETIONS_TEMPLATE, sqlDialect, partialSql)
        ));

        // 创建请求对象
        String modelName = Constants.AiModel.DEEPSEEK.equalsIgnoreCase(modelType) 
            ? deepSeekProperties.getModel() 
            : openAiProperties.getModel();
            
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(messages)
                .temperature(Constants.AiModel.CREATIVE_TEMPERATURE) // 适当提高温度以获得多样化的建议
                .maxTokens(Constants.AiModel.DEFAULT_MAX_TOKENS)
                .build();

        try {
            String response;

            // 根据模型类型选择服务
            if (Constants.AiModel.DEEPSEEK.equalsIgnoreCase(modelType)) {
                response = deepseekService.createChatCompletion(completionRequest);
            } else {
                ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
                response = result.getChoices().get(0).getMessage().getContent();
            }

            // 解析响应为多行
            List<String> completions = new ArrayList<>();
            if (response != null && !response.trim().isEmpty()) {
                // 清理可能存在的代码块标记
                response = cleanupResponse(response);
                
                // 按行分割
                String[] lines = response.split("\\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        completions.add(trimmed);
                    }
                }
            }
            
            logger.info(MessageResolver.format(Constants.LogMessages.COMPLETIONS_GENERATED, partialSql, completions.size()));
            return completions;
        } catch (Exception e) {
            logger.error(Constants.ErrorMessages.SQL_COMPLETIONS_ERROR, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getSqlCompletions(String partialSql, String dialect, String schemaInfo) {
        // 默认使用openai作为模型类型
        return getSqlCompletions(partialSql, dialect, schemaInfo, Constants.AiModel.OPENAI);
    }

    @Override
    public List<Map<String, String>> getSqlExamples(String dialect, String schemaInfo, String modelType) {
        // 准备聊天消息
        List<ChatMessage> messages = new ArrayList<>();

        // 系统消息，告诉AI它的角色和任务
        messages.add(new ChatMessage(
            Constants.AiModel.SYSTEM_ROLE, 
            aiProperties.getSystemPrompts().get("sql-assistant")
        ));

        // 添加数据库结构信息，格式化结构以便AI更好理解
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            messages.add(new ChatMessage(
                Constants.AiModel.SYSTEM_ROLE,
                MessageResolver.format(Constants.PromptTemplates.SCHEMA_INFO_TEMPLATE, formatSchemaInfo(schemaInfo))
            ));
        }

        // 用户查询，提供明确的任务说明
        String sqlDialect = dialect != null ? dialect : Constants.Sql.DEFAULT_DIALECT;
        messages.add(new ChatMessage(
            Constants.AiModel.USER_ROLE,
            MessageResolver.format(Constants.PromptTemplates.SQL_EXAMPLES_TEMPLATE, sqlDialect)
        ));

        // 创建请求对象
        String modelName = Constants.AiModel.DEEPSEEK.equalsIgnoreCase(modelType) 
            ? deepSeekProperties.getModel() 
            : openAiProperties.getModel();
            
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(modelName)
                .messages(messages)
                .temperature(Constants.AiModel.CREATIVE_TEMPERATURE) // 适当提高温度以获得多样化的示例
                .maxTokens(Constants.AiModel.DEFAULT_MAX_TOKENS)
                .build();

        try {
            String response;

            // 根据模型类型选择服务
            if (Constants.AiModel.DEEPSEEK.equalsIgnoreCase(modelType)) {
                response = deepseekService.createChatCompletion(completionRequest);
            } else {
                ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
                response = result.getChoices().get(0).getMessage().getContent();
            }

            // 解析JSON响应
            List<Map<String, String>> examples = new ArrayList<>();
            if (response != null && !response.trim().isEmpty()) {
                // 清理可能存在的代码块和其他格式
                response = cleanupResponse(response);
                
                try {
                    // 如果响应不是标准JSON，尝试提取JSON部分
                    if (!response.trim().startsWith("[")) {
                        Pattern jsonPattern = Pattern.compile(Constants.AiModel.JSON_ARRAY_PATTERN, Pattern.DOTALL);
                        Matcher matcher = jsonPattern.matcher(response);
                        if (matcher.find()) {
                            response = matcher.group(0);
                        }
                    }
                    
                    // 使用Jackson解析JSON
                    examples = new ObjectMapper().readValue(
                        response, 
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {}
                    );
                    
                    // 确保每个示例都包含description和sql字段
                    examples = examples.stream()
                        .filter(map -> map.containsKey("description") && map.containsKey("sql"))
                        .peek(map -> map.put("sql", sqlFormatterService.formatSql(map.get("sql"), sqlDialect)))
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    logger.error(Constants.ErrorMessages.PARSE_JSON_ERROR, e);
                    // 如果JSON解析失败，返回空列表
                    return new ArrayList<>();
                }
            }
            
            logger.info(MessageResolver.format(Constants.LogMessages.EXAMPLES_GENERATED, sqlDialect, examples.size()));
            return examples;
        } catch (Exception e) {
            logger.error(Constants.ErrorMessages.SQL_EXAMPLES_ERROR, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, String>> getSqlExamples(String dialect, String schemaInfo) {
        // 默认使用openai作为模型类型
        return getSqlExamples(dialect, schemaInfo, Constants.AiModel.OPENAI);
    }
}
package com.dbmanage.api.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.theokanning.openai.client.OpenAiApi;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI模型服务
 * 处理自然语言转换为SQL
 * 支持OpenAI和DeepSeek模型
 */
@Service
public class NlpToSqlService {

    private final OpenAiService openAiService;
    private final String openaiModel;
    private final int openaiTimeoutSeconds;
    
    private final DeepSeekService deepseekService;
    private final String deepseekModel;
    private final int deepseekTimeoutSeconds;
    
    private final SqlFormatterService sqlFormatterService;

    /**
     * 构造函数
     * 
     * @param openaiApiKey OpenAI API密钥
     * @param openaiModel OpenAI模型名称
     * @param openaiTimeoutSeconds OpenAI请求超时时间（秒）
     * @param openaiBaseUrl OpenAI基础URL
     * @param deepseekApiKey DeepSeek API密钥
     * @param deepseekModel DeepSeek模型名称
     * @param deepseekTimeoutSeconds DeepSeek请求超时时间（秒）
     * @param sqlFormatterService SQL格式化服务
     */
    public NlpToSqlService(
            @Value("${openai.api-key:}") String openaiApiKey,
            @Value("${openai.model:gpt-3.5-turbo}") String openaiModel,
            @Value("${openai.timeout-seconds:30}") int openaiTimeoutSeconds,
            @Value("${openai.base-url:https://api.openai.com/v1/}") String openaiBaseUrl,
            @Value("${deepseek.api-key:}") String deepseekApiKey,
            @Value("${deepseek.model:deepseek-chat}") String deepseekModel,
            @Value("${deepseek.timeout-seconds:30}") int deepseekTimeoutSeconds,
            SqlFormatterService sqlFormatterService) {
        
        // 创建OpenAiService，使用token认证
        this.openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(openaiTimeoutSeconds));
        
        this.openaiModel = openaiModel;
        this.openaiTimeoutSeconds = openaiTimeoutSeconds;
        
        this.deepseekService = new DeepSeekService(deepseekApiKey, deepseekModel, Duration.ofSeconds(deepseekTimeoutSeconds));
        this.deepseekModel = deepseekModel;
        this.deepseekTimeoutSeconds = deepseekTimeoutSeconds;
        
        this.sqlFormatterService = sqlFormatterService;
    }
    
    /**
     * 将自然语言查询转换为SQL
     * 
     * @param naturalLanguageQuery 自然语言查询
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息（表和列）
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return 生成的SQL
     */
    public String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo, String modelType) {
        // 准备聊天消息
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统消息，告诉AI它的角色和任务
        messages.add(new ChatMessage("system", 
                "你是一个专业的数据库SQL转换专家，能够将自然语言准确地转换为高质量的SQL查询语句。遵循以下规则：\n\n" +
                "1. 仅返回SQL代码，不要包含解释、注释或其他非SQL内容\n" +
                "2. 生成的SQL必须是可执行的，符合指定SQL方言的标准语法\n" +
                "3. 仔细分析提供的数据库结构信息，确保使用正确的表名和字段名\n" +
                "4. 理解自然语言查询的意图，选择合适的JOIN、WHERE条件和聚合函数\n" +
                "5. 使用恰当的类型转换和比较操作\n" +
                "6. 确保使用的表和字段确实存在于提供的数据库结构中\n" +
                "7. 当涉及到日期时间时，根据需要使用正确的日期函数\n" +
                "8. 当不确定字段或表的具体含义时，基于表和字段的注释信息做出最合理的推断\n\n" +
                "9. 重要：始终使用表和字段的英文名称（name属性）而不是中文名称（comment属性）来编写SQL查询\n" +
                "10. 即使用户在自然语言查询中使用了表或字段的中文名称，也要在SQL中使用对应的英文名称\n" +
                "11. 必须且只能使用提供的数据库结构中存在的表和字段，不要使用任何未在提供的架构中列出的表或字段\n" +
                "12. 如果查询意图中提到的表或字段在提供的数据库结构中找不到，使用最接近的匹配或在注释中提示无法找到\n\n" +
                "数据库结构说明：\n" +
                "- 数据库结构遵循以下层次结构：数据库 -> 表 -> 字段\n" +
                "- 每个表(table)都有name(表英文名)和comment(表中文名或描述)属性\n" +
                "- SQL语句中必须使用表的英文名(name)，而不是表的中文名(comment)\n" +
                "- 每个表包含多个字段(columns)，每个字段都有以下属性：\n" +
                "  * name: 字段英文名\n" +
                "  * type: 字段数据类型\n" +
                "  * comment: 字段中文名或描述\n" +
                "  * nullable: 是否允许为空\n" +
                "  * isPrimaryKey: 是否为主键\n" +
                "- 表与表之间的关系通常通过主键(isPrimaryKey=true)和外键字段建立\n" +
                "- 表的comment和字段的comment包含了业务含义，请充分利用这些注释理解数据模型\n\n" +
                "13. 若需要分页，使用标准的LIMIT和OFFSET或方言特定的语法\n\n" +
                "方言类型: " + (dialect != null ? dialect : "标准SQL")));
        
        // 添加数据库结构信息，格式化结构以便AI更好理解
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            messages.add(new ChatMessage("system", 
                    "下面是数据库结构信息，包含表、表注释、字段、字段注释等关键信息。请仔细分析每个表的结构、含义和关系：\n\n" + 
                    formatSchemaInfo(schemaInfo) + "\n\n" +
                    "在生成SQL时：\n" +
                    "1. 确保只使用上述结构中存在的表和字段\n" +
                    "2. 必须使用表和字段的英文名(name)而不是中文名或描述(comment)\n" +
                    "3. 尽可能利用表和字段的注释信息来理解它们的实际含义，但在SQL中一定要使用英文名\n" +
                    "4. 针对字段名或其含义有疑问时，参考注释选择最符合查询意图的解释\n" +
                    "5. 如果表之间存在关系，分析可能的关联字段：\n" +
                    "   - 主键(isPrimaryKey=true)与其他表中同名或类似字段\n" +
                    "   - 命名模式如：表名_id的字段通常是外键\n" +
                    "   - 具有相同数据类型且业务意义相关的字段\n" +
                    "6. 表的命名和注释可能暗示表之间的层次结构，如主表/子表关系\n" +
                    "7. 如果自然语言查询中提到的表或字段在上述结构中不存在，请选择最相似的表或字段或返回无法执行的提示"));
        }
        
        // 用户查询，提供明确的任务说明
        messages.add(new ChatMessage("user", 
                "请将以下自然语言描述转换为" + (dialect != null ? dialect : "SQL") + "查询：\n\n" + 
                naturalLanguageQuery + "\n\n" +
                "只需要返回SQL代码，不要包含任何解释或额外信息。请根据我提供的数据库结构信息，生成最准确的SQL查询。"));
        
        // 创建请求对象
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("deepseek".equalsIgnoreCase(modelType) ? deepseekModel : openaiModel)
                .messages(messages)
                .temperature(0.1) // 降低多样性，使结果更确定性
                .maxTokens(800) // 增加最大token数，以应对复杂查询
                .build();
        
        try {
            String sqlResponse;
            
            // 根据模型类型选择服务
            if ("deepseek".equalsIgnoreCase(modelType)) {
                sqlResponse = deepseekService.createChatCompletion(completionRequest);
            } else {
                ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
                sqlResponse = result.getChoices().get(0).getMessage().getContent();
            }
            
            // 去除可能的```sql和```标记
            sqlResponse = cleanupResponse(sqlResponse);
            
            // 使用SQL格式化工具格式化SQL
            if (dialect != null && !dialect.isEmpty()) {
                return sqlFormatterService.formatSql(sqlResponse, dialect);
            } else {
                return sqlFormatterService.formatSql(sqlResponse, "sql");
            }
        } catch (Exception e) {
            throw new RuntimeException("无法生成SQL: " + e.getMessage(), e);
        }
    }
    
    /**
     * 格式化数据库结构信息，使其更易于AI理解
     * 
     * @param schemaInfo 原始的数据库结构信息JSON字符串
     * @return 格式化后的文本
     */
    private String formatSchemaInfo(String schemaInfo) {
        try {
            // 看看schemaInfo是不是已经是格式化的JSON
            if (schemaInfo.trim().startsWith("{") && schemaInfo.contains("tables")) {
                // 尝试将schemaInfo解析为更可读的格式
                StringBuilder builder = new StringBuilder();
                
                // 这里可以使用JSON解析库来处理，比如Jackson
                // 为了简单起见，我们使用基本的字符串处理
                
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
                            String columnsSection = tablePart.substring(columnsStart);
                            // 简单解析columns数组
                            String[] columnParts = columnsSection.split("\\{\"name\":");
                            
                            for (int j = 1; j < columnParts.length && j < 100; j++) { // 限制处理100个字段
                                String columnPart = columnParts[j];
                                String columnName = extractValue(columnPart, "name");
                                String columnType = extractValue(columnPart, "type");
                                String columnComment = extractValue(columnPart, "comment");
                                String nullable = extractValue(columnPart, "nullable");
                                String key = extractValue(columnPart, "key");
                                
                                builder.append("  - ").append(columnName);
                                if (columnType != null && !columnType.isEmpty()) {
                                    builder.append(" (类型: ").append(columnType).append(")");
                                }
                                if (columnComment != null && !columnComment.isEmpty()) {
                                    builder.append(" 说明: ").append(columnComment);
                                }
                                if ("PRI".equals(key) || "主键".equals(key)) {
                                    builder.append(" [主键]");
                                }
                                if ("false".equals(nullable)) {
                                    builder.append(" [不可为空]");
                                }
                                builder.append("\n");
                            }
                        }
                        builder.append("\n");
                    }
                }
                
                return builder.toString();
            }
            
            // 如果不是预期的格式，返回原始字符串
            return schemaInfo;
        } catch (Exception e) {
            // 出现异常时返回原始字符串
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
    
    /**
     * 重载原方法，保持向后兼容
     */
    public String generateSql(String naturalLanguageQuery, String dialect, String schemaInfo) {
        return generateSql(naturalLanguageQuery, dialect, schemaInfo, "openai");
    }
    
    /**
     * 清理AI响应中可能包含的代码块标记和其他非SQL内容
     * 
     * @param response AI的响应
     * @return 清理后的SQL
     */
    private String cleanupResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "";
        }
        
        // 移除可能的Markdown代码块标记
        response = response.replaceAll("```sql", "").replaceAll("```", "");
        
        // 移除可能的解释性文本（通常在SQL之前或之后）
        String[] lines = response.split("\n");
        StringBuilder sqlBuilder = new StringBuilder();
        boolean inSqlBlock = false;
        boolean foundSql = false;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // 跳过空行
            if (trimmedLine.isEmpty()) {
                continue;
            }
            
            // 跳过注释行
            if (trimmedLine.startsWith("--") || trimmedLine.startsWith("#") || trimmedLine.startsWith("/*")) {
                continue;
            }
            
            // 检测是否进入SQL语句块
            if (!inSqlBlock && !foundSql) {
                // 通常SQL语句以SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP等关键字开头
                if (trimmedLine.toUpperCase().startsWith("SELECT") || 
                    trimmedLine.toUpperCase().startsWith("INSERT") || 
                    trimmedLine.toUpperCase().startsWith("UPDATE") || 
                    trimmedLine.toUpperCase().startsWith("DELETE") || 
                    trimmedLine.toUpperCase().startsWith("CREATE") || 
                    trimmedLine.toUpperCase().startsWith("ALTER") || 
                    trimmedLine.toUpperCase().startsWith("DROP") ||
                    trimmedLine.toUpperCase().startsWith("WITH")) {
                    inSqlBlock = true;
                    foundSql = true;
                }
            }
            
            // 如果在SQL块中，添加这一行（排除可能嵌入的注释）
            if (inSqlBlock && !trimmedLine.startsWith("--") && !trimmedLine.startsWith("#")) {
                sqlBuilder.append(line).append("\n");
            }
        }
        
        // 如果没有找到SQL块，返回原始响应去除代码块标记后的版本
        if (!foundSql) {
            return response.trim();
        }
        
        return sqlBuilder.toString().trim();
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
    public List<String> getSqlCompletions(String partialSql, String dialect, String schemaInfo, String modelType) {
        // 准备聊天消息
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统消息
        messages.add(new ChatMessage("system", 
                "你是一个SQL自动补全助手。用户将提供一个不完整的SQL查询，你需要提供可能的补全选项。" +
                "返回一个JSON数组，包含3-5个最可能的补全选项。每个选项应该是可以直接附加到用户输入后的SQL片段。" +
                "格式为: [\"选项1\", \"选项2\", \"选项3\"]"));
        
        // 添加数据库结构信息
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            messages.add(new ChatMessage("system", 
                    "下面是数据库结构:\n" + schemaInfo));
        }
        
        // 用户的不完整SQL
        messages.add(new ChatMessage("user", 
                "给我以下SQL的补全选项：\n" + partialSql));
        
        // 创建请求对象
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("deepseek".equalsIgnoreCase(modelType) ? deepseekModel : openaiModel)
                .messages(messages)
                .temperature(0.3)
                .maxTokens(200)
                .build();
        
        try {
            String completionsResponse;
            
            // 根据模型类型选择服务
            if ("deepseek".equalsIgnoreCase(modelType)) {
                completionsResponse = deepseekService.createChatCompletion(completionRequest);
            } else {
                ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
                completionsResponse = result.getChoices().get(0).getMessage().getContent();
            }
            
            // 解析JSON响应
            return parseCompletionsResponse(completionsResponse);
        } catch (Exception e) {
            throw new RuntimeException("无法获取SQL补全: " + e.getMessage(), e);
        }
    }
    
    /**
     * 重载原方法，保持向后兼容
     */
    public List<String> getSqlCompletions(String partialSql, String dialect, String schemaInfo) {
        return getSqlCompletions(partialSql, dialect, schemaInfo, "openai");
    }
    
    /**
     * 解析AI返回的补全建议
     * 
     * @param response AI的响应
     * @return 补全建议列表
     */
    private List<String> parseCompletionsResponse(String response) {
        List<String> completions = new ArrayList<>();
        
        // 简单解析，假设响应是一个JSON数组格式
        response = response.trim();
        if (response.startsWith("[") && response.endsWith("]")) {
            response = response.substring(1, response.length() - 1);
            
            // 分割字符串并清理
            String[] items = response.split(",");
            for (String item : items) {
                item = item.trim();
                if (item.startsWith("\"") && item.endsWith("\"")) {
                    item = item.substring(1, item.length() - 1);
                    completions.add(item);
                }
            }
        }
        
        // 如果解析失败，至少返回原始响应
        if (completions.isEmpty()) {
            completions.add(response);
        }
        
        return completions;
    }
    
    /**
     * 获取可能的SQL查询示例
     * 
     * @param dialect SQL方言
     * @param schemaInfo 数据库结构信息
     * @param modelType 模型类型 (openai 或 deepseek)
     * @return 查询示例
     */
    public List<Map<String, String>> getSqlExamples(String dialect, String schemaInfo, String modelType) {
        // 准备聊天消息
        List<ChatMessage> messages = new ArrayList<>();
        
        // 系统消息
        messages.add(new ChatMessage("system", 
                "生成5个有用的SQL示例查询，基于提供的数据库结构。" +
                "每个示例应该包含一个自然语言描述和对应的SQL查询。" +
                "返回格式为JSON数组，每项包含'description'和'sql'字段。"));
        
        // 添加数据库结构信息
        if (schemaInfo != null && !schemaInfo.isEmpty()) {
            messages.add(new ChatMessage("user", 
                    "基于以下数据库结构，生成5个有用的SQL示例查询：\n" + schemaInfo));
        } else {
            messages.add(new ChatMessage("user", 
                    "生成5个常用SQL示例查询，包括SELECT、INSERT、UPDATE等操作。"));
        }
        
        // 创建请求对象
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("deepseek".equalsIgnoreCase(modelType) ? deepseekModel : openaiModel)
                .messages(messages)
                .temperature(0.5)
                .maxTokens(800)
                .build();
        
        try {
            String examplesResponse;
            
            // 根据模型类型选择服务
            if ("deepseek".equalsIgnoreCase(modelType)) {
                examplesResponse = deepseekService.createChatCompletion(completionRequest);
            } else {
                ChatCompletionResult result = openAiService.createChatCompletion(completionRequest);
                examplesResponse = result.getChoices().get(0).getMessage().getContent();
            }
            
            // 解析JSON响应
            return parseExamplesResponse(examplesResponse, dialect);
        } catch (Exception e) {
            // 如果API调用失败，返回一些基本示例
            return getDefaultExamples(dialect);
        }
    }
    
    /**
     * 重载原方法，保持向后兼容
     */
    public List<Map<String, String>> getSqlExamples(String dialect, String schemaInfo) {
        return getSqlExamples(dialect, schemaInfo, "openai");
    }
    
    /**
     * 解析AI返回的SQL示例
     */
    private List<Map<String, String>> parseExamplesResponse(String response, String dialect) {
        List<Map<String, String>> examples = new ArrayList<>();
        
        // 简单解析，提取示例
        // 实际项目中应该使用JSON解析库如Jackson
        String[] lines = response.split("\\n");
        
        Map<String, String> currentExample = null;
        StringBuilder sqlBuilder = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("{") || line.contains("\"description\"")) {
                // 开始新示例
                if (currentExample != null && !currentExample.isEmpty()) {
                    if (sqlBuilder.length() > 0) {
                        String sql = sqlBuilder.toString().trim();
                        // 格式化SQL
                        sql = sqlFormatterService.formatSql(sql, dialect != null ? dialect : "sql");
                        currentExample.put("sql", sql);
                        sqlBuilder = new StringBuilder();
                    }
                    examples.add(currentExample);
                }
                currentExample = new HashMap<>();
            } else if (line.contains("\"description\"") || line.contains("描述")) {
                if (currentExample != null) {
                    String desc = line.replaceAll("\"description\"\\s*:\\s*\"", "")
                            .replaceAll("\",$", "").trim();
                    currentExample.put("description", desc);
                }
            } else if (line.contains("\"sql\"") || line.contains("SQL")) {
                // SQL开始
                // 清空之前的内容准备收集新SQL
                sqlBuilder = new StringBuilder();
            } else if (currentExample != null && !line.isEmpty() && !line.equals("}") && !line.equals("],")) {
                // 收集SQL内容
                sqlBuilder.append(line).append(" ");
            }
        }
        
        // 添加最后一个示例
        if (currentExample != null && !currentExample.isEmpty()) {
            if (sqlBuilder.length() > 0) {
                String sql = sqlBuilder.toString().trim();
                sql = sqlFormatterService.formatSql(sql, dialect != null ? dialect : "sql");
                currentExample.put("sql", sql);
            }
            examples.add(currentExample);
        }
        
        // 如果解析失败，提供默认示例
        if (examples.isEmpty()) {
            return getDefaultExamples(dialect);
        }
        
        return examples;
    }
    
    /**
     * 获取默认SQL示例
     */
    private List<Map<String, String>> getDefaultExamples(String dialect) {
        List<Map<String, String>> examples = new ArrayList<>();
        
        // 示例1：基本查询
        Map<String, String> example1 = new HashMap<>();
        example1.put("description", "查询表中的所有数据");
        example1.put("sql", sqlFormatterService.formatSql("SELECT * FROM table_name", dialect));
        examples.add(example1);
        
        // 示例2：条件查询
        Map<String, String> example2 = new HashMap<>();
        example2.put("description", "根据条件查询数据");
        example2.put("sql", sqlFormatterService.formatSql(
                "SELECT column1, column2 FROM table_name WHERE condition = value", dialect));
        examples.add(example2);
        
        // 示例3：聚合查询
        Map<String, String> example3 = new HashMap<>();
        example3.put("description", "分组统计查询");
        example3.put("sql", sqlFormatterService.formatSql(
                "SELECT column1, COUNT(*) FROM table_name GROUP BY column1", dialect));
        examples.add(example3);
        
        // 示例4：多表联查
        Map<String, String> example4 = new HashMap<>();
        example4.put("description", "多表关联查询");
        example4.put("sql", sqlFormatterService.formatSql(
                "SELECT a.column1, b.column2 FROM table1 a JOIN table2 b ON a.id = b.ref_id", dialect));
        examples.add(example4);
        
        // 示例5：插入数据
        Map<String, String> example5 = new HashMap<>();
        example5.put("description", "插入新数据");
        example5.put("sql", sqlFormatterService.formatSql(
                "INSERT INTO table_name (column1, column2) VALUES (value1, value2)", dialect));
        examples.add(example5);
        
        return examples;
    }
} 
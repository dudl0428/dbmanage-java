package com.dbmanage.api.common;

/**
 * 系统常量类
 * 用于存放系统中的各种常量
 */
public class Constants {

    /**
     * API相关常量
     */
    public static class Api {
        public static final String CONTENT_TYPE_JSON = "application/json";
        public static final String BEARER_PREFIX = "Bearer ";
    }
    
    /**
     * AI模型相关常量
     */
    public static class AiModel {
        public static final String OPENAI = "openai";
        public static final String DEEPSEEK = "deepseek";
        
        public static final double DEFAULT_TEMPERATURE = 0.1;
        public static final double CREATIVE_TEMPERATURE = 0.7;
        public static final int DEFAULT_MAX_TOKENS = 800;
        
        public static final String SYSTEM_ROLE = "system";
        public static final String USER_ROLE = "user";
        public static final String ASSISTANT_ROLE = "assistant";
        
        public static final String CODE_BLOCK_PATTERN = "```(?:sql)?([\\s\\S]*?)```";
        public static final String JSON_ARRAY_PATTERN = "\\[.*\\]";
    }
    
    /**
     * SQL相关常量
     */
    public static class Sql {
        public static final String DEFAULT_DIALECT = "sql";
    }
    
    /**
     * 错误消息常量
     */
    public static class ErrorMessages {
        public static final String GENERATE_SQL_ERROR = "无法生成SQL: {0}";
        public static final String PARSE_JSON_ERROR = "解析SQL示例JSON时出错";
        public static final String SQL_COMPLETIONS_ERROR = "生成SQL补全建议时出错";
        public static final String SQL_EXAMPLES_ERROR = "生成SQL示例时出错";
    }
    
    /**
     * 日志消息常量
     */
    public static class LogMessages {
        public static final String INIT_SERVICE = "初始化NlpToSqlService完成，使用模型: OpenAI={0}, DeepSeek={1}";
        public static final String COMPLETIONS_GENERATED = "为SQL片段 '{0}' 生成了 {1} 个补全建议";
        public static final String EXAMPLES_GENERATED = "为{0}方言生成了 {1} 个SQL示例";
    }
    
    /**
     * AI提示模板相关常量
     */
    public static class PromptTemplates {
        
        public static final String SCHEMA_INFO_TEMPLATE = 
                "下面是数据库结构信息，包含表、表注释、字段、字段注释等关键信息。请仔细分析每个表的结构、含义和关系：\n\n" +
                "{0}\n\n" +
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
                "7. 如果自然语言查询中提到的表或字段在上述结构中不存在，请选择最相似的表或字段或返回无法执行的提示";

        public static final String NL_TO_SQL_TEMPLATE = 
                "请将以下自然语言描述转换为{0}查询：\n\n" +
                "{1}\n\n" +
                "只需要返回SQL代码，不要包含任何解释或额外信息。请根据我提供的数据库结构信息，生成最准确的SQL查询。";
                
        public static final String SQL_COMPLETIONS_TEMPLATE =
                "为以下SQL片段提供5个可能的补全建议，每个都以不同的方式完成查询。只返回SQL补全部分，每个补全占一行，不要有其他任何说明。\n\n" +
                "SQL片段 ({0}方言): {1}";
                
        public static final String SQL_EXAMPLES_TEMPLATE =
                "请生成5个常用SQL查询示例，针对{0}方言和提供的数据库结构。每个示例包括:\n" +
                "1. 简短描述：用一句话说明这个查询的用途\n" +
                "2. SQL语句：完整可执行的SQL\n\n" +
                "请以JSON数组格式返回，每个对象包含description和sql两个字段。不要包含其他说明文字。";
    }
} 
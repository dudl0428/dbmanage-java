# DeepSeek模型支持文档

## 概述

AI数据库管理系统已经集成了DeepSeek大语言模型支持，可以用于自然语言转SQL、SQL补全以及生成SQL示例等功能。系统使用DeepSeek专用API客户端，直接与DeepSeek API通信。

## 配置说明

DeepSeek模型的配置参数在`application.yml`文件中：

```yaml
# DeepSeek配置
deepseek:
  api-key: your-deepseek-api-key
  model: deepseek-chat
  timeout-seconds: 30
```

请确保替换`your-deepseek-api-key`为您的实际DeepSeek API密钥。

### 支持的模型

DeepSeek提供多种模型，推荐使用以下模型：

- `deepseek-chat`: 通用对话模型，适合大多数SQL生成场景
- `deepseek-coder`: 代码生成模型，更适合复杂SQL生成

## API使用方法

系统支持在以下接口中使用DeepSeek模型：

### 1. 自然语言转SQL

```
POST /api/ai/generate-sql
```

参数:
- `query`: 自然语言查询 (必填)
- `dialect`: SQL方言 (可选，默认: "sql")
- `schemaInfo`: 数据库结构信息 (可选)
- `modelType`: 模型类型 (可选，默认: "deepseek")

示例请求:
```
POST /api/ai/generate-sql?query=查询所有用户的名字和邮箱&modelType=deepseek
```

### 2. SQL补全

```
POST /api/ai/sql-completions
```

参数:
- `partialSql`: 部分SQL (必填)
- `dialect`: SQL方言 (可选，默认: "sql")
- `schemaInfo`: 数据库结构信息 (可选)
- `modelType`: 模型类型 (可选，默认: "deepseek")

示例请求:
```
POST /api/ai/sql-completions?partialSql=SELECT * FROM users WHERE&modelType=deepseek
```

### 3. SQL示例

```
GET /api/ai/sql-examples
```

参数:
- `dialect`: SQL方言 (可选，默认: "sql")
- `schemaInfo`: 数据库结构信息 (可选)
- `modelType`: 模型类型 (可选，默认: "deepseek")

示例请求:
```
GET /api/ai/sql-examples?modelType=deepseek
```

## DeepSeek API实现说明

系统使用自定义的`DeepSeekService`类来调用DeepSeek API，而不是使用OpenAI的SDK。这确保了与DeepSeek API的完全兼容。

主要API端点：
- 聊天补全: `https://api.deepseek.com/v1/chat/completions`

通过HTTP请求体发送以下参数：
```json
{
  "model": "deepseek-chat",
  "messages": [
    {"role": "system", "content": "系统提示"},
    {"role": "user", "content": "用户查询"}
  ],
  "temperature": 0.1,
  "max_tokens": 500
}
```

## DeepSeek模型的优势

DeepSeek模型在SQL生成方面具有以下优势：

1. 对SQL语法的深度理解
2. 专为中文用户优化，支持更好的中文自然语言查询
3. 针对代码生成进行了优化
4. 对数据库结构和关系的良好把握
5. 支持多种SQL方言和复杂查询结构

## 注意事项

- 确保提供足够的数据库结构信息以获得最佳结果
- 对于复杂的SQL生成，推荐使用DeepSeek模型
- API调用有超时设置，默认为30秒
- 确保DeepSeek API密钥有效且具有足够的配额
- 当提供数据库结构信息时，尽量包含表名、列名和列类型，以及表之间的关系

## 故障排除

如果遇到DeepSeek API调用问题，请检查：

1. API密钥是否正确
2. 网络连接是否正常
3. 请求体格式是否符合DeepSeek API要求
4. 日志中是否有详细的错误信息 
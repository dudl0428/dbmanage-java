package com.dbmanage.api.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API服务类
 * 用于调用DeepSeek的大语言模型API
 */
public class DeepSeekService {

    private static final String API_BASE_URL = "https://api.deepseek.com";
    private static final String CHAT_URL = API_BASE_URL + "/v1/chat/completions";
    
    private final String apiKey;
    private final String model;
    private final Duration timeout;
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    
    /**
     * 构造函数
     * 
     * @param apiKey DeepSeek API密钥
     * @param model 模型名称
     * @param timeout 超时时间
     */
    public DeepSeekService(String apiKey, String model, Duration timeout) {
        this.apiKey = apiKey;
        this.model = model;
        this.timeout = timeout;
        
        // 配置HTTP客户端
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
        
        // 配置JSON映射器
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
    
    /**
     * 执行聊天补全请求
     * 
     * @param messages 聊天消息列表
     * @param temperature 采样温度
     * @param maxTokens 最大生成token数量
     * @return 生成的回复内容
     * @throws IOException 调用API时可能抛出IO异常
     */
    public String createChatCompletion(List<ChatMessage> messages, double temperature, int maxTokens) throws IOException {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        
        // 转换为JSON
        String json = mapper.writeValueAsString(requestBody);
        
        // 构建HTTP请求
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .url(CHAT_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        // 执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("DeepSeek API请求失败: " + response.code() + " - " + response.message());
            }
            
            // 解析响应
            String responseBody = response.body().string();
            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
            
            // 提取生成的内容
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    return (String) message.get("content");
                }
            }
            
            throw new IOException("无法从DeepSeek API响应中提取内容");
        }
    }
    
    /**
     * 使用聊天补全请求对象执行请求
     * 
     * @param request 聊天补全请求对象
     * @return 生成的回复内容
     * @throws IOException 调用API时可能抛出IO异常
     */
    public String createChatCompletion(ChatCompletionRequest request) throws IOException {
        return createChatCompletion(
                request.getMessages(), 
                request.getTemperature(), 
                request.getMaxTokens());
    }
} 
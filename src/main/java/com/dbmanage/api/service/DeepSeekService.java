package com.dbmanage.api.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.io.IOException;
import java.util.List;

/**
 * DeepSeek API服务接口
 * 用于调用DeepSeek的大语言模型API
 */
public interface DeepSeekService {

    /**
     * 执行聊天补全请求
     *
     * @param messages 聊天消息列表
     * @param temperature 采样温度
     * @param maxTokens 最大生成token数量
     * @return 生成的回复内容
     * @throws IOException 调用API时可能抛出IO异常
     */
    String createChatCompletion(List<ChatMessage> messages, double temperature, int maxTokens) throws IOException;

    /**
     * 使用聊天补全请求对象执行请求
     *
     * @param request 聊天补全请求对象
     * @return 生成的回复内容
     * @throws IOException 调用API时可能抛出IO异常
     */
    String createChatCompletion(ChatCompletionRequest request) throws IOException;
}
package com.dbmanage.api.service;

import com.theokanning.openai.completion.chat.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeepSeek服务测试类
 * 注意：这些测试不会真正调用DeepSeek API，而是验证服务类的基本功能
 */
public class DeepSeekServiceTest {
    
    private DeepSeekService deepSeekService;
    private final String TEST_API_KEY = "test-api-key";
    private final String TEST_MODEL = "deepseek-chat";
    private final Duration TEST_TIMEOUT = Duration.ofSeconds(30);
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        deepSeekService = new DeepSeekService(TEST_API_KEY, TEST_MODEL, TEST_TIMEOUT);
    }
    
    /**
     * 测试构造函数
     */
    @Test
    public void testConstructor() {
        assertNotNull(deepSeekService, "DeepSeekService实例不应为空");
    }
    
    /**
     * 测试聊天补全方法参数传递
     * 注意：由于无法真正调用API，此测试只验证方法是否正确处理参数并抛出预期的异常
     */
    @Test
    public void testCreateChatCompletionParameters() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "你是SQL助手"));
        messages.add(new ChatMessage("user", "生成查询用户表的SQL"));
        
        double temperature = 0.1;
        int maxTokens = 500;
        
        Exception exception = assertThrows(IOException.class, () -> {
            deepSeekService.createChatCompletion(messages, temperature, maxTokens);
        });
        
        // 验证是否抛出了IO异常，因为没有真正的HTTP连接
        assertTrue(exception.getMessage().contains("Failed to connect") 
                || exception.getMessage().contains("Connection refused")
                || exception.getMessage().contains("failed") 
                || exception.getMessage().contains("Unable to resolve host"),
                "应抛出连接相关异常");
    }
    
    /**
     * 测试创建聊天补全请求对象
     */
    @Test
    public void testCreateChatCompletionWithRequestObject() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "你是SQL助手"));
        messages.add(new ChatMessage("user", "生成查询用户表的SQL"));
        
        com.theokanning.openai.completion.chat.ChatCompletionRequest request = 
                com.theokanning.openai.completion.chat.ChatCompletionRequest.builder()
                .model(TEST_MODEL)
                .messages(messages)
                .temperature(0.1)
                .maxTokens(500)
                .build();
        
        Exception exception = assertThrows(IOException.class, () -> {
            deepSeekService.createChatCompletion(request);
        });
        
        // 验证是否抛出了IO异常，因为没有真正的HTTP连接
        assertTrue(exception.getMessage().contains("Failed to connect") 
                || exception.getMessage().contains("Connection refused")
                || exception.getMessage().contains("failed")
                || exception.getMessage().contains("Unable to resolve host"),
                "应抛出连接相关异常");
    }
} 
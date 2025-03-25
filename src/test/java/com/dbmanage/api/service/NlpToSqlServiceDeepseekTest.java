package com.dbmanage.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek模型支持测试类
 */
public class NlpToSqlServiceDeepseekTest {

    @Mock
    private SqlFormatterService sqlFormatterService;

    @InjectMocks
    private NlpToSqlService nlpToSqlService;
    
    private final String TEST_SQL = "SELECT * FROM users";
    private final String FORMATTED_SQL = "SELECT * FROM users";
    private final String TEST_SCHEMA = "users(id, name, email)";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // 模拟SqlFormatterService的行为
        when(sqlFormatterService.formatSql(anyString(), anyString())).thenAnswer(i -> i.getArgument(0));
        when(sqlFormatterService.formatSql(eq(TEST_SQL), anyString())).thenReturn(FORMATTED_SQL);
    }

    /**
     * 测试DeepSeek模型类型检测
     */
    @Test
    public void testDeepseekModelTypeDetection() {
        // 无法真正测试API调用，但可以验证代码中的模型类型检测逻辑
        Exception exception = null;
        
        try {
            // 这个调用会失败，因为我们没有实际的API连接，但我们可以捕获异常并验证它不是由于模型类型检测错误引起的
            nlpToSqlService.generateSql("查询所有用户", "sql", TEST_SCHEMA, "deepseek");
        } catch (Exception e) {
            exception = e;
            // 应该是由于API调用失败而不是模型类型错误
            assertTrue(e.getMessage().contains("无法生成SQL") || e.getMessage().contains("API") || e.getMessage().contains("请求"), 
                    "异常应该是由于API调用失败，而不是模型类型错误");
        }
        
        // 验证确实抛出了异常（因为没有实际的API连接）
        assertNotNull(exception, "应该抛出异常，因为没有实际的API连接");
    }
    
    /**
     * 测试DeepSeek模型参数传递
     */
    @Test
    public void testDeepseekModelParameter() {
        try {
            // 测试generateSql方法支持deepseek模型参数
            Exception exception = null;
            try {
                nlpToSqlService.generateSql("查询所有用户", "sql", TEST_SCHEMA, "deepseek");
            } catch (Exception e) {
                exception = e;
                assertFalse(e.getMessage().contains("modelType"), "异常不应该与modelType参数有关");
            }
            
            // 测试getSqlCompletions方法支持deepseek模型参数
            exception = null;
            try {
                nlpToSqlService.getSqlCompletions("SELECT * FROM", "sql", TEST_SCHEMA, "deepseek");
            } catch (Exception e) {
                exception = e;
                assertFalse(e.getMessage().contains("modelType"), "异常不应该与modelType参数有关");
            }
            
            // 测试getSqlExamples方法支持deepseek模型参数
            exception = null;
            try {
                nlpToSqlService.getSqlExamples("sql", TEST_SCHEMA, "deepseek");
            } catch (Exception e) {
                exception = e;
                assertFalse(e.getMessage().contains("modelType"), "异常不应该与modelType参数有关");
            }
        } catch (Exception e) {
            fail("测试执行过程中出现意外异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试默认示例功能
     */
    @Test
    public void testGetDefaultExamples() {
        try {
            List<Map<String, String>> examples = nlpToSqlService.getSqlExamples("sql", "", "deepseek");
            
            // 即使API调用失败，也应该返回默认示例
            assertNotNull(examples, "应该返回默认示例列表");
            assertFalse(examples.isEmpty(), "示例列表不应为空");
            
            // 验证示例格式
            for (Map<String, String> example : examples) {
                assertTrue(example.containsKey("description"), "示例应包含描述");
                assertTrue(example.containsKey("sql"), "示例应包含SQL");
                assertNotNull(example.get("description"), "描述不应为空");
                assertNotNull(example.get("sql"), "SQL不应为空");
            }
        } catch (Exception e) {
            fail("测试执行过程中出现意外异常: " + e.getMessage());
        }
    }
} 
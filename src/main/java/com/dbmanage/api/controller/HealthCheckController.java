package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.common.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

import static com.dbmanage.api.common.ApiResponse.success;

/**
 * 健康检查控制器
 * 提供API状态检查端点
 */
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/health")
public class HealthCheckController extends BaseController {

    /**
     * 健康检查接口，提供API可用性验证
     * @return API状态信息
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("service", "数据库管理工具API");
        status.put("version", "1.0.0");
        return success(status);
    }
}
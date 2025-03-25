package com.dbmanage.api.controller;

import com.dbmanage.api.common.ApiResponse;
import com.dbmanage.api.dto.CreateTableRequest;
import com.dbmanage.api.dto.ValidateFieldRequest;
import com.dbmanage.api.exception.ValidationException;
import com.dbmanage.api.service.DatabaseTypeService;
import com.dbmanage.api.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/table")
public class TableController {
    @Resource
    private  TableService tableService;
    @Resource
    private  DatabaseTypeService databaseTypeService;

    
    @PostMapping("/validate-field")
    public ResponseEntity<ApiResponse<Void>> validateField(@RequestBody ValidateFieldRequest request) {
        // 请求参数校验
        if (request == null || request.getField() == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "无效的请求参数")
            );
        }

        try {
            tableService.validateField(request);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), e.getField()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error(500, "字段验证发生未预期错误: " + e.getMessage())
            );
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validateTable(@RequestBody CreateTableRequest request) {
        // 请求参数校验
        if (request == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "无效的请求参数")
            );
        }

        try {
            tableService.validateTable(request);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), e.getField()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error(500, "表验证发生未预期错误: " + e.getMessage())
            );
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createTable(@RequestBody CreateTableRequest request) {
        // 请求参数校验
        if (request == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "无效的请求参数")
            );
        }

        // 表名不能为空
        if (!StringUtils.hasText(request.getTableName())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "表名不能为空", "tableName")
            );
        }

        // 检查字段列表
        if (request.getFields() == null || request.getFields().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "表至少需要包含一个字段", "fields")
            );
        }

        try {
            tableService.createTable(request);
            return ResponseEntity.ok(ApiResponse.success());
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), e.getField()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error(500, "创建表发生未预期错误: " + e.getMessage())
            );
        }
    }
    
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabaseTypes(
            @RequestParam(required = true) String databaseType,
            @RequestParam(required = false) String connectionId) {
        // 参数校验
        if (!StringUtils.hasText(databaseType)) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "数据库类型不能为空")
            );
        }

        System.out.println("接收到获取数据类型请求: databaseType=" + databaseType + ", connectionId=" + connectionId);

        try {
            // 转换为小写，统一处理
            String dbType = databaseType.toLowerCase();
            
            // 检查是否支持该数据库类型
            if (!databaseTypeService.isSupportedDatabase(dbType)) {
                System.out.println("不支持的数据库类型: " + dbType);
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(400, "不支持的数据库类型: " + databaseType)
                );
            }

            // 获取数据类型列表
            Map<String, Object> types = databaseTypeService.getSupportedTypes(dbType);
            System.out.println("获取到数据类型: " + (types != null ? types.size() : "null") + " 项");
            
            // 检查是否获取到数据类型
            if (types == null || types.isEmpty()) {
                System.out.println("未找到数据类型");
                return ResponseEntity.ok(
                    ApiResponse.success("未找到数据类型", new HashMap<>())
                );
            }

            // 添加类型数量信息用于日志
            Object typesList = types.get("types");
            int typesCount = (typesList instanceof List) ? ((List<?>) typesList).size() : 0;
            System.out.println("返回数据类型成功: " + typesCount + " 种类型");

            return ResponseEntity.ok(
                ApiResponse.success("获取数据类型成功", types)
            );

        } catch (IllegalArgumentException e) {
            // 参数错误
            System.out.println("无效的数据库类型: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "无效的数据库类型: " + e.getMessage())
            );
        } catch (UnsupportedOperationException e) {
            // 不支持的操作
            System.out.println("数据库类型暂不支持: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error(501, "数据库类型暂不支持: " + e.getMessage())
            );
        } catch (Exception e) {
            // 其他未预期的错误
            System.out.println("获取数据类型失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                ApiResponse.error(500, "获取数据类型失败: " + e.getMessage())
            );
        }
    }
} 
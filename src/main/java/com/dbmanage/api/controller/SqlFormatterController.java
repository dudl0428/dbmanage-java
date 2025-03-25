package com.dbmanage.api.controller;

import com.dbmanage.api.service.SqlFormatterService;
import com.dbmanage.api.dto.SqlFormatRequest;
import com.dbmanage.api.dto.SqlFormatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

/**
 * SQL格式化接口
 */
@RestController
@RequestMapping("/sql")
public class SqlFormatterController {

    private final SqlFormatterService sqlFormatterService;

    @Autowired
    public SqlFormatterController(SqlFormatterService sqlFormatterService) {
        this.sqlFormatterService = sqlFormatterService;
    }

    /**
     * 简单SQL格式化
     *
     * @param request 格式化请求
     * @return 格式化后的SQL
     */
    @PostMapping("/format")
    public ResponseEntity<SqlFormatResponse> formatSql(@Valid @RequestBody SqlFormatRequest request) {
        String formattedSql = sqlFormatterService.formatSql(
                request.getSql(),
                request.getDialect() != null ? request.getDialect() : "sql"
        );

        return ResponseEntity.ok(new SqlFormatResponse(formattedSql));
    }

    /**
     * 高级SQL格式化（带选项）
     *
     * @param request 格式化请求
     * @param options 高级格式化选项
     * @return 格式化后的SQL
     */
    @PostMapping("/format/advanced")
    public ResponseEntity<SqlFormatResponse> formatSqlWithOptions(
            @Valid @RequestBody SqlFormatRequest request,
            @RequestParam(required = false) Map<String, Object> options) {
        
        String formattedSql = sqlFormatterService.formatSqlWithOptions(
                request.getSql(),
                request.getDialect() != null ? request.getDialect() : "sql",
                options != null ? options : Collections.emptyMap()
        );

        return ResponseEntity.ok(new SqlFormatResponse(formattedSql));
    }
} 
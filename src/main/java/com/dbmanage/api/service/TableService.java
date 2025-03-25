package com.dbmanage.api.service;

import com.dbmanage.api.dto.CreateTableRequest;
import com.dbmanage.api.dto.ValidateFieldRequest;
import com.dbmanage.api.exception.ValidationException;

import java.sql.SQLException;

/**
 * 表管理服务接口
 * 处理数据库表的创建、验证等操作
 */
public interface TableService {

    /**
     * 验证字段
     * @param request 字段验证请求
     */
    void validateField(ValidateFieldRequest request);

    /**
     * 验证表
     * @param request 创建表请求
     */
    void validateTable(CreateTableRequest request);

    /**
     * 创建表
     * @param request 创建表请求
     * @throws SQLException 数据库操作异常
     * @throws ValidationException 表结构验证异常
     */
    void createTable(CreateTableRequest request) throws SQLException, ValidationException;
}
package com.dbmanage.api.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据库类型服务接口
 * 用于处理不同数据库类型的特定操作和类型信息
 */

public interface DatabaseTypeService {

    /**
     * 检查数据库类型是否支持
     * 
     * @param dbType 数据库类型
     * @return 是否支持
     */
    boolean isSupportedDatabase(String dbType);

    /**
     * 获取支持的数据库类型及其字段类型
     * 
     * @param dbType 数据库类型
     * @return 支持的类型信息
     */
    Map<String, Object> getSupportedTypes(String dbType);

    /**
     * 获取指定数据库支持的字段类型列表
     * 
     * @param databaseType 数据库类型
     * @return 支持的字段类型列表
     */
    List<String> getSupportedTypesList(String databaseType);

    /**
     * 检查指定数据库的字段类型是否支持长度设置
     * 
     * @param databaseType 数据库类型
     * @param type 字段类型
     * @return 是否支持长度
     */
    boolean typeSupportsLength(String databaseType, String type);

    /**
     * 获取指定数据库字段类型的最大长度
     * 
     * @param databaseType 数据库类型
     * @param type 字段类型
     * @return 最大长度
     */
    Integer getTypeMaxLength(String databaseType, String type);

    /**
     * 检查指定数据库的字段类型是否支持小数设置
     * 
     * @param databaseType 数据库类型
     * @param type 字段类型
     * @return 是否支持小数
     */
    boolean typeSupportsDecimal(String databaseType, String type);

    /**
     * 获取指定数据库字段类型的最大小数位数
     * 
     * @param databaseType 数据库类型
     * @param type 字段类型
     * @return 最大小数位数
     */
    Integer getTypeMaxDecimal(String databaseType, String type);

    /**
     * 检查指定数据库的字段类型是否支持自增
     * 
     * @param databaseType 数据库类型
     * @param type 字段类型
     * @return 是否支持自增
     */
    boolean typeSupportsAutoIncrement(String databaseType, String type);

    /**
     * 获取标准化后的数据库类型名称
     * 
     * @param databaseType 原始数据库类型名称
     * @return 标准化后的数据库类型名称
     */
    String getNormalizedDatabaseType(String databaseType);
}
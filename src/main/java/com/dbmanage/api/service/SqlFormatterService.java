package com.dbmanage.api.service;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL格式化服务
 * 使用sql-formatter库实现SQL格式化功能
 */

public interface SqlFormatterService {
    /**
     * 格式化SQL
     *
     * @param sql     SQL语句
     * @param dialect SQL方言类型
     * @return 格式化后的SQL
     */
     String formatSql(String sql, String dialect);

    /**
     * 使用高级选项格式化SQL
     *
     * @param sql     SQL语句
     * @param dialect SQL方言类型
     * @param options 格式化选项
     * @return 格式化后的SQL
     */
     String formatSqlWithOptions(String sql, String dialect, Map<String, Object> options);

} 
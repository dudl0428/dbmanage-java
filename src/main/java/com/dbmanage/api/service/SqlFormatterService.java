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
@Service
public class SqlFormatterService {

    /**
     * 方言映射表
     */
    private static final Map<String, Dialect> DIALECT_MAP = new HashMap<>();

    static {
        DIALECT_MAP.put("sql", Dialect.StandardSql);
        DIALECT_MAP.put("mysql", Dialect.MySql);
        DIALECT_MAP.put("postgresql", Dialect.PostgreSql);
        DIALECT_MAP.put("tsql", Dialect.TSql);
        DIALECT_MAP.put("mariadb", Dialect.MariaDb);
        DIALECT_MAP.put("db2", Dialect.Db2);
        DIALECT_MAP.put("plsql", Dialect.PlSql);
        DIALECT_MAP.put("n1ql", Dialect.N1ql);
        DIALECT_MAP.put("redshift", Dialect.Redshift);
        DIALECT_MAP.put("spark", Dialect.SparkSql);
    }

    /**
     * 格式化SQL
     *
     * @param sql     SQL语句
     * @param dialect SQL方言类型
     * @return 格式化后的SQL
     */
    public String formatSql(String sql, String dialect) {
        try {
            Dialect dialectType = DIALECT_MAP.getOrDefault(dialect.toLowerCase(), Dialect.StandardSql);
            return SqlFormatter.of(dialectType).format(sql);
        } catch (Exception e) {
            // 如果格式化失败，返回原始SQL
            return sql;
        }
    }

    /**
     * 使用高级选项格式化SQL
     *
     * @param sql     SQL语句
     * @param dialect SQL方言类型
     * @param options 格式化选项
     * @return 格式化后的SQL
     */
    public String formatSqlWithOptions(String sql, String dialect, Map<String, Object> options) {
        try {
            Dialect dialectType = DIALECT_MAP.getOrDefault(dialect.toLowerCase(), Dialect.StandardSql);
            
            // 使用默认配置的格式化器
            return SqlFormatter.of(dialectType).format(sql);
            
        } catch (Exception e) {
            // 如果格式化失败，返回原始SQL
            return sql;
        }
    }
} 
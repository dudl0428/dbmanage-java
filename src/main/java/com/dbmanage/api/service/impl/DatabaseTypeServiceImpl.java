package com.dbmanage.api.service.impl;

import com.dbmanage.api.service.DatabaseTypeService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DatabaseTypeServiceImpl implements DatabaseTypeService {

    // 支持的数据库类型
    private static final Set<String> SUPPORTED_DATABASES = new HashSet<>(
            Arrays.asList("mysql", "postgresql", "sqlserver", "oracle", "sqlite", "mariadb")
    );

    // 存储各数据库类型支持的数据类型
    private final Map<String, List<String>> databaseTypes = new HashMap<>();

    // 存储数据类型的特性（长度、精度等）
    private final Map<String, Map<String, Object>> typeProperties = new HashMap<>();

    public DatabaseTypeServiceImpl() {
        initDatabaseTypes();
    }

    /**
     * 初始化数据库类型信息
     */
    private void initDatabaseTypes() {
        // MySQL 数据类型
        List<String> mysqlTypes = Arrays.asList(
                "int", "tinyint", "smallint", "mediumint", "bigint",
                "float", "double", "decimal",
                "char", "varchar", "tinytext", "text", "mediumtext", "longtext",
                "date", "datetime", "timestamp", "time", "year",
                "blob", "tinyblob", "mediumblob", "longblob",
                "enum", "set", "json", "boolean"
        );
        databaseTypes.put("mysql", mysqlTypes);

        // 为MySQL类型添加特性
        Map<String, Object> mysqlTypeProps = new HashMap<>();
        // 数值类型
        defineTypeProperties(mysqlTypeProps, "int", true, false, 11, null, true);
        defineTypeProperties(mysqlTypeProps, "tinyint", true, false, 4, null, true);
        defineTypeProperties(mysqlTypeProps, "smallint", true, false, 6, null, true);
        defineTypeProperties(mysqlTypeProps, "mediumint", true, false, 9, null, true);
        defineTypeProperties(mysqlTypeProps, "bigint", true, false, 20, null, true);

        // 浮点数类型
        defineTypeProperties(mysqlTypeProps, "float", true, true, 24, 8, false);
        defineTypeProperties(mysqlTypeProps, "double", true, true, 53, 15, false);
        defineTypeProperties(mysqlTypeProps, "decimal", true, true, 65, 30, false);

        // 字符串类型
        defineTypeProperties(mysqlTypeProps, "char", true, false, 255, null, false);
        defineTypeProperties(mysqlTypeProps, "varchar", true, false, 65535, null, false);
        defineTypeProperties(mysqlTypeProps, "tinytext", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "text", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "mediumtext", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "longtext", false, false, null, null, false);

        // 日期时间类型
        defineTypeProperties(mysqlTypeProps, "date", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "datetime", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "timestamp", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "time", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "year", false, false, null, null, false);

        // 二进制类型
        defineTypeProperties(mysqlTypeProps, "blob", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "tinyblob", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "mediumblob", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "longblob", false, false, null, null, false);

        // 其他类型
        defineTypeProperties(mysqlTypeProps, "enum", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "set", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "json", false, false, null, null, false);
        defineTypeProperties(mysqlTypeProps, "boolean", false, false, null, null, false);

        typeProperties.put("mysql", mysqlTypeProps);

        // PostgreSQL 数据类型 (简化版)
        List<String> postgresqlTypes = Arrays.asList(
                "smallint", "integer", "bigint", "serial", "bigserial",
                "real", "double precision", "numeric",
                "char", "varchar", "text",
                "date", "time", "timestamp", "interval",
                "bytea", "boolean", "json", "jsonb"
        );
        databaseTypes.put("postgresql", postgresqlTypes);

        // SQL Server 数据类型 (简化版)
        List<String> sqlserverTypes = Arrays.asList(
                "bigint", "bit", "decimal", "int", "money", "numeric", "smallint", "smallmoney", "tinyint",
                "float", "real",
                "char", "varchar", "text", "nchar", "nvarchar", "ntext",
                "date", "datetime", "datetime2", "datetimeoffset", "smalldatetime", "time",
                "binary", "varbinary", "image"
        );
        databaseTypes.put("sqlserver", sqlserverTypes);

        // Oracle 数据类型 (简化版)
        List<String> oracleTypes = Arrays.asList(
                "number", "float", "binary_float", "binary_double",
                "char", "varchar2", "nchar", "nvarchar2", "long", "clob", "nclob",
                "date", "timestamp", "interval year to month", "interval day to second",
                "raw", "long raw", "blob", "bfile"
        );
        databaseTypes.put("oracle", oracleTypes);

        // SQLite 数据类型 (简化版)
        List<String> sqliteTypes = Arrays.asList(
                "integer", "real", "text", "blob", "numeric"
        );
        databaseTypes.put("sqlite", sqliteTypes);

        // MariaDB 与 MySQL 类似，可以复用
        databaseTypes.put("mariadb", mysqlTypes);
        typeProperties.put("mariadb", mysqlTypeProps);
    }

    /**
     * 定义数据类型的特性
     */
    private void defineTypeProperties(Map<String, Object> typeProps, String type,
                                      boolean supportsLength, boolean supportsDecimal,
                                      Integer maxLength, Integer maxDecimal,
                                      boolean supportsAutoIncrement) {
        Map<String, Object> props = new HashMap<>();
        props.put("supportsLength", supportsLength);
        props.put("supportsDecimal", supportsDecimal);
        props.put("maxLength", maxLength);
        props.put("maxDecimal", maxDecimal);
        props.put("supportsAutoIncrement", supportsAutoIncrement);
        typeProps.put(type.toLowerCase(), props);
    }

    /**
     * 检查数据库类型是否支持
     */
    public boolean isSupportedDatabase(String databaseType) {
        return SUPPORTED_DATABASES.contains(databaseType.toLowerCase());
    }

    /**
     * 获取数据库支持的类型列表
     */
    public List<String> getSupportedTypesList(String databaseType) {
        String normalizedType = getNormalizedDatabaseType(databaseType);
        return databaseTypes.getOrDefault(normalizedType, Collections.emptyList());
    }

    /**
     * 获取数据库支持的类型和类型定义
     */
    public Map<String, Object> getSupportedTypes(String databaseType) {
        String normalizedType = getNormalizedDatabaseType(databaseType);

        Map<String, Object> result = new HashMap<>();
        result.put("types", databaseTypes.getOrDefault(normalizedType, Collections.emptyList()));

        // 获取类型定义
        Map<String, Object> definitions = new HashMap<>();
        Map<String, Object> dbTypeProps = typeProperties.getOrDefault(normalizedType, Collections.emptyMap());

        if (!dbTypeProps.isEmpty()) {
            for (String type : databaseTypes.getOrDefault(normalizedType, Collections.emptyList())) {
                definitions.put(type, dbTypeProps.getOrDefault(type.toLowerCase(), Collections.emptyMap()));
            }
        }

        result.put("definitions", definitions);
        return result;
    }

    /**
     * 标准化数据库类型名称
     */
    public String getNormalizedDatabaseType(String databaseType) {
        if (databaseType == null) {
            return "";
        }
        String normalized = databaseType.toLowerCase();
        if (SUPPORTED_DATABASES.contains(normalized)) {
            return normalized;
        }
        // 处理别名
        if ("postgres".equals(normalized)) {
            return "postgresql";
        }
        if ("mssql".equals(normalized)) {
            return "sqlserver";
        }
        return normalized;
    }

    /**
     * 检查类型是否支持长度属性
     */
    public boolean typeSupportsLength(String databaseType, String typeName) {
        if (typeName == null) {
            return false;
        }

        String normalizedDbType = getNormalizedDatabaseType(databaseType);
        Map<String, Object> dbTypeProps = typeProperties.getOrDefault(normalizedDbType, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> typeProps = (Map<String, Object>) dbTypeProps.get(typeName.toLowerCase());
        if (typeProps == null) {
            return false;
        }

        return Boolean.TRUE.equals(typeProps.get("supportsLength"));
    }

    /**
     * 检查类型是否支持小数位属性
     */
    public boolean typeSupportsDecimal(String databaseType, String typeName) {
        if (typeName == null) {
            return false;
        }

        String normalizedDbType = getNormalizedDatabaseType(databaseType);
        Map<String, Object> dbTypeProps = typeProperties.getOrDefault(normalizedDbType, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> typeProps = (Map<String, Object>) dbTypeProps.get(typeName.toLowerCase());
        if (typeProps == null) {
            return false;
        }

        return Boolean.TRUE.equals(typeProps.get("supportsDecimal"));
    }

    /**
     * 检查类型是否支持自增
     */
    public boolean typeSupportsAutoIncrement(String databaseType, String typeName) {
        if (typeName == null) {
            return false;
        }

        String normalizedDbType = getNormalizedDatabaseType(databaseType);
        Map<String, Object> dbTypeProps = typeProperties.getOrDefault(normalizedDbType, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> typeProps = (Map<String, Object>) dbTypeProps.get(typeName.toLowerCase());
        if (typeProps == null) {
            return false;
        }

        return Boolean.TRUE.equals(typeProps.get("supportsAutoIncrement"));
    }

    /**
     * 获取类型的最大长度
     */
    public Integer getTypeMaxLength(String databaseType, String typeName) {
        if (typeName == null) {
            return null;
        }

        String normalizedDbType = getNormalizedDatabaseType(databaseType);
        Map<String, Object> dbTypeProps = typeProperties.getOrDefault(normalizedDbType, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> typeProps = (Map<String, Object>) dbTypeProps.get(typeName.toLowerCase());
        if (typeProps == null) {
            return null;
        }

        return (Integer) typeProps.get("maxLength");
    }

    /**
     * 获取类型的最大小数位数
     */
    public Integer getTypeMaxDecimal(String databaseType, String typeName) {
        if (typeName == null) {
            return null;
        }

        String normalizedDbType = getNormalizedDatabaseType(databaseType);
        Map<String, Object> dbTypeProps = typeProperties.getOrDefault(normalizedDbType, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> typeProps = (Map<String, Object>) dbTypeProps.get(typeName.toLowerCase());
        if (typeProps == null) {
            return null;
        }

        return (Integer) typeProps.get("maxDecimal");
    }
}

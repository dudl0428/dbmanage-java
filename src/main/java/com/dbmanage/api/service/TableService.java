package com.dbmanage.api.service;

import com.dbmanage.api.dto.CreateTableRequest;
import com.dbmanage.api.dto.FieldDefinition;
import com.dbmanage.api.dto.ForeignKeyDefinition;
import com.dbmanage.api.dto.IndexDefinition;
import com.dbmanage.api.dto.ValidateFieldRequest;
import com.dbmanage.api.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableService {
    private static final Logger logger = LoggerFactory.getLogger(TableService.class);
    
    private final ConnectionService connectionService;
    private final DatabaseTypeService databaseTypeService;
    
    public TableService(ConnectionService connectionService, DatabaseTypeService databaseTypeService) {
        this.connectionService = connectionService;
        this.databaseTypeService = databaseTypeService;
    }
    
    /**
     * 验证字段定义
     */
    public void validateField(ValidateFieldRequest request) throws ValidationException {
        if (request == null || request.getField() == null) {
            throw new ValidationException("字段信息不能为空");
        }
        
        FieldDefinition field = request.getField();
        
        // 验证字段名
        if (!StringUtils.hasText(field.getName())) {
            throw new ValidationException("字段名不能为空", "name");
        }
        
        // 验证字段名格式
        if (!field.getName().matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new ValidationException("字段名只能包含字母、数字和下划线，且不能以数字开头", "name");
        }
        
        // 验证字段类型
        if (!StringUtils.hasText(field.getType())) {
            throw new ValidationException("字段类型不能为空", "type");
        }
        
        // 验证数据类型是否受支持
        List<String> supportedTypes = databaseTypeService.getSupportedTypesList(request.getDatabaseType());
        if (!supportedTypes.contains(field.getType().toLowerCase())) {
            throw new ValidationException("不支持的数据类型: " + field.getType(), "type");
        }
        
        // 验证字段长度（针对特定类型）
        if (databaseTypeService.typeSupportsLength(request.getDatabaseType(), field.getType())) {
            if (field.getLength() != null && field.getLength() <= 0) {
                throw new ValidationException("字段长度必须大于0", "length");
            }
            
            // 验证最大长度
            Integer maxLength = databaseTypeService.getTypeMaxLength(request.getDatabaseType(), field.getType());
            if (maxLength != null && field.getLength() != null && field.getLength() > maxLength) {
                throw new ValidationException("字段长度不能超过 " + maxLength, "length");
            }
        }
        
        // 验证小数位数（针对特定类型）
        if (databaseTypeService.typeSupportsDecimal(request.getDatabaseType(), field.getType())) {
            if (field.getDecimal() != null && field.getDecimal() < 0) {
                throw new ValidationException("小数位数不能为负数", "decimal");
            }
            
            // 验证最大小数位数
            Integer maxDecimal = databaseTypeService.getTypeMaxDecimal(request.getDatabaseType(), field.getType());
            if (maxDecimal != null && field.getDecimal() != null && field.getDecimal() > maxDecimal) {
                throw new ValidationException("小数位数不能超过 " + maxDecimal, "decimal");
            }
            
            // 验证小数位数不能大于总长度
            if (field.getLength() != null && field.getDecimal() != null && field.getDecimal() >= field.getLength()) {
                throw new ValidationException("小数位数不能大于或等于总长度", "decimal");
            }
        }
        
        // 验证自增选项（仅允许数值类型）
        if (field.isAutoIncrement() && !databaseTypeService.typeSupportsAutoIncrement(request.getDatabaseType(), field.getType())) {
            throw new ValidationException("数据类型 " + field.getType() + " 不支持自增", "autoIncrement");
        }
        
        logger.info("字段验证成功: {}", field.getName());
    }
    
    /**
     * 验证整张表的定义
     */
    public void validateTable(CreateTableRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("请求不能为空");
        }
        
        // 验证表名
        if (!StringUtils.hasText(request.getTableName())) {
            throw new ValidationException("表名不能为空", "tableName");
        }
        
        // 验证表名格式
        if (!request.getTableName().matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new ValidationException("表名只能包含字母、数字和下划线，且不能以数字开头", "tableName");
        }
        
        // 验证字段列表
        if (request.getFields() == null || request.getFields().isEmpty()) {
            throw new ValidationException("至少需要一个字段", "fields");
        }
        
        // 验证每个字段
        List<String> fieldNames = new ArrayList<>();
        for (FieldDefinition field : request.getFields()) {
            // 使用 ValidateFieldRequest 重用验证逻辑
            ValidateFieldRequest fieldRequest = new ValidateFieldRequest();
            fieldRequest.setField(field);
            fieldRequest.setDatabaseType(request.getDatabaseType());
            fieldRequest.setConnectionId(request.getConnectionId());
            validateField(fieldRequest);
            
            // 检查字段名重复
            if (fieldNames.contains(field.getName().toLowerCase())) {
                throw new ValidationException("字段名 '" + field.getName() + "' 重复", "fields");
            }
            fieldNames.add(field.getName().toLowerCase());
        }
        
        // 检查主键
        boolean hasPrimaryKey = request.getFields().stream().anyMatch(FieldDefinition::isPrimaryKey);
        if (!hasPrimaryKey) {
            logger.warn("表 {} 没有设置主键", request.getTableName());
            // 这里只是警告，不阻止表的创建
        }
        
        // 验证索引
        if (request.getIndexes() != null) {
            request.getIndexes().forEach(index -> {
                if (!StringUtils.hasText(index.getName())) {
                    throw new ValidationException("索引名不能为空", "indexes");
                }
                
                if (index.getColumnNames() == null || index.getColumnNames().isEmpty()) {
                    throw new ValidationException("索引 '" + index.getName() + "' 必须包含至少一个字段", "indexes");
                }
                
                // 验证索引字段存在于表中
                for (String columnName : index.getColumnNames()) {
                    if (!fieldNames.contains(columnName.toLowerCase())) {
                        throw new ValidationException("索引 '" + index.getName() + "' 引用了不存在的字段 '" + columnName + "'", "indexes");
                    }
                }
            });
        }
        
        // 验证外键
        if (request.getForeignKeys() != null) {
            request.getForeignKeys().forEach(fk -> {
                if (!StringUtils.hasText(fk.getName())) {
                    throw new ValidationException("外键名不能为空", "foreignKeys");
                }
                
                if (fk.getSourceColumns() == null || fk.getSourceColumns().isEmpty()) {
                    throw new ValidationException("外键 '" + fk.getName() + "' 必须包含至少一个源字段", "foreignKeys");
                }
                
                if (!StringUtils.hasText(fk.getReferenceTable())) {
                    throw new ValidationException("外键 '" + fk.getName() + "' 必须指定引用表", "foreignKeys");
                }
                
                if (fk.getReferenceColumns() == null || fk.getReferenceColumns().isEmpty()) {
                    throw new ValidationException("外键 '" + fk.getName() + "' 必须包含至少一个引用字段", "foreignKeys");
                }
                
                // 验证源字段存在于表中
                for (String columnName : fk.getSourceColumns()) {
                    if (!fieldNames.contains(columnName.toLowerCase())) {
                        throw new ValidationException("外键 '" + fk.getName() + "' 引用了不存在的源字段 '" + columnName + "'", "foreignKeys");
                    }
                }
                
                // 源字段和引用字段数量必须匹配
                if (fk.getSourceColumns().size() != fk.getReferenceColumns().size()) {
                    throw new ValidationException("外键 '" + fk.getName() + "' 源字段和引用字段数量不匹配", "foreignKeys");
                }
            });
        }
        
        logger.info("表定义验证成功: {}", request.getTableName());
    }
    
    /**
     * 创建表
     */
    @Transactional
    public void createTable(CreateTableRequest request) throws SQLException, ValidationException {
        // 先验证表定义
        validateTable(request);
        
        // 获取数据库连接
        try (Connection connection = connectionService.getConnection(request.getConnectionId())) {
            // 构建 SQL
            String createTableSql = buildCreateTableSql(request);
            logger.info("创建表 SQL: {}", createTableSql);
            
            // 执行 SQL
            try (PreparedStatement stmt = connection.prepareStatement(createTableSql)) {
                stmt.executeUpdate();
            }
            
            // 创建索引
            if (request.getIndexes() != null && !request.getIndexes().isEmpty()) {
                for (int i = 0; i < request.getIndexes().size(); i++) {
                    String createIndexSql = buildCreateIndexSql(request.getTableName(), request.getIndexes().get(i));
                    logger.info("创建索引 SQL: {}", createIndexSql);
                    
                    try (PreparedStatement stmt = connection.prepareStatement(createIndexSql)) {
                        stmt.executeUpdate();
                    }
                }
            }
            
            // 创建外键
            if (request.getForeignKeys() != null && !request.getForeignKeys().isEmpty()) {
                for (int i = 0; i < request.getForeignKeys().size(); i++) {
                    String createFkSql = buildCreateForeignKeySql(request.getTableName(), request.getForeignKeys().get(i));
                    logger.info("创建外键 SQL: {}", createFkSql);
                    
                    try (PreparedStatement stmt = connection.prepareStatement(createFkSql)) {
                        stmt.executeUpdate();
                    }
                }
            }
            
            logger.info("表 {} 创建成功", request.getTableName());
        }
    }
    
    /**
     * 构建创建表的 SQL
     */
    private String buildCreateTableSql(CreateTableRequest request) {
        StringBuilder sql = new StringBuilder();
        
        // 不同数据库类型的 SQL 语法有差异
        String dbType = databaseTypeService.getNormalizedDatabaseType(request.getDatabaseType());
        
        sql.append("CREATE TABLE ");
        
        // 添加表名
        sql.append(escapeTableName(request.getTableName(), dbType));
        sql.append(" (");
        
        // 添加字段定义
        List<String> columnDefinitions = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        for (FieldDefinition field : request.getFields()) {
            StringBuilder colSql = new StringBuilder();
            
            // 字段名
            colSql.append(escapeColumnName(field.getName(), dbType)).append(" ");
            
            // 字段类型
            colSql.append(getColumnTypeDefinition(field, dbType));
            
            // NOT NULL
            if (field.isNotNull()) {
                colSql.append(" NOT NULL");
            }
            
            // 自增
            if (field.isAutoIncrement()) {
                colSql.append(getAutoIncrementSyntax(dbType));
            }
            
            // 注释
            if (StringUtils.hasText(field.getComment())) {
                colSql.append(getColumnCommentSyntax(field.getComment(), dbType));
            }
            
            columnDefinitions.add(colSql.toString());
            
            // 收集主键
            if (field.isPrimaryKey()) {
                primaryKeys.add(escapeColumnName(field.getName(), dbType));
            }
        }
        
        // 添加主键约束
        if (!primaryKeys.isEmpty()) {
            columnDefinitions.add("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
        }
        
        // 合并所有字段定义
        sql.append(String.join(", ", columnDefinitions));
        sql.append(")");
        
        // 表注释
        if (StringUtils.hasText(request.getComment())) {
            sql.append(getTableCommentSyntax(request.getComment(), dbType));
        }
        
        return sql.toString();
    }
    
    /**
     * 构建创建索引的 SQL
     */
    private String buildCreateIndexSql(String tableName, IndexDefinition index) {
        StringBuilder sql = new StringBuilder();
        
        // 获取数据库类型
        String dbType = databaseTypeService.getNormalizedDatabaseType(index.getDatabaseType());
            
            sql.append("CREATE ");
        
        // 索引类型
            if (index.isUnique()) {
                sql.append("UNIQUE ");
            }
            
            sql.append("INDEX ");
        sql.append(escapeIndexName(index.getName(), dbType));
            sql.append(" ON ");
        sql.append(escapeTableName(tableName, dbType));
        sql.append(" (");
        
        // 索引字段
        List<String> columns = index.getColumnNames().stream()
                .map(col -> escapeColumnName(col, dbType))
                .collect(Collectors.toList());
        
        sql.append(String.join(", ", columns));
            sql.append(")");
        
        return sql.toString();
    }
    
    /**
     * 构建创建外键的 SQL
     */
    private String buildCreateForeignKeySql(String tableName, ForeignKeyDefinition fk) {
        StringBuilder sql = new StringBuilder();
        
        // 获取数据库类型
        String dbType = databaseTypeService.getNormalizedDatabaseType(fk.getDatabaseType());
            
            sql.append("ALTER TABLE ");
        sql.append(escapeTableName(tableName, dbType));
            sql.append(" ADD CONSTRAINT ");
        sql.append(escapeForeignKeyName(fk.getName(), dbType));
            sql.append(" FOREIGN KEY (");
        
        // 外键源字段
        List<String> sourceColumns = fk.getSourceColumns().stream()
                .map(col -> escapeColumnName(col, dbType))
                .collect(Collectors.toList());
        
        sql.append(String.join(", ", sourceColumns));
            sql.append(") REFERENCES ");
        sql.append(escapeTableName(fk.getReferenceTable(), dbType));
            sql.append(" (");
        
        // 外键引用字段
        List<String> refColumns = fk.getReferenceColumns().stream()
                .map(col -> escapeColumnName(col, dbType))
                .collect(Collectors.toList());
        
        sql.append(String.join(", ", refColumns));
            sql.append(")");
            
        // 更新规则
        if (StringUtils.hasText(fk.getUpdateRule())) {
            sql.append(" ON UPDATE ").append(fk.getUpdateRule());
        }
        
        // 删除规则
        if (StringUtils.hasText(fk.getDeleteRule())) {
            sql.append(" ON DELETE ").append(fk.getDeleteRule());
        }
        
        return sql.toString();
    }
    
    // 辅助方法：获取字段类型定义字符串
    private String getColumnTypeDefinition(FieldDefinition field, String dbType) {
        StringBuilder typeDef = new StringBuilder(field.getType());
        
        // 不同数据库处理长度和小数位不同
        if (databaseTypeService.typeSupportsLength(dbType, field.getType())) {
            if (field.getLength() != null) {
                if (databaseTypeService.typeSupportsDecimal(dbType, field.getType()) && field.getDecimal() != null) {
                    // 带精度的类型
                    typeDef.append("(").append(field.getLength()).append(",").append(field.getDecimal()).append(")");
                } else {
                    // 普通长度类型
                    typeDef.append("(").append(field.getLength()).append(")");
                }
            }
        }
        
        return typeDef.toString();
    }
    
    // 辅助方法：获取自增语法
    private String getAutoIncrementSyntax(String dbType) {
        switch (dbType) {
            case "mysql":
                return " AUTO_INCREMENT";
            case "postgresql":
                return ""; // PostgreSQL 使用特殊的类型如 SERIAL
            case "sqlserver":
                return " IDENTITY(1,1)";
            case "oracle":
                return ""; // Oracle 使用序列
            default:
                return " AUTO_INCREMENT";
        }
    }
    
    // 辅助方法：获取字段注释语法
    private String getColumnCommentSyntax(String comment, String dbType) {
        switch (dbType) {
            case "mysql":
                return " COMMENT '" + escapeSqlString(comment) + "'";
            case "postgresql":
                // PostgreSQL 需要单独的语句
                return "";
            case "sqlserver":
                // SQL Server 需要单独的语句
                return "";
            case "oracle":
                // Oracle 需要单独的语句
                return "";
            default:
                return "";
        }
    }
    
    // 辅助方法：获取表注释语法
    private String getTableCommentSyntax(String comment, String dbType) {
        switch (dbType) {
            case "mysql":
                return " COMMENT='" + escapeSqlString(comment) + "'";
            case "postgresql":
            case "sqlserver":
            case "oracle":
                // 需要单独的语句
                return "";
            default:
                return "";
        }
    }
    
    // 辅助方法：转义表名
    private String escapeTableName(String tableName, String dbType) {
        switch (dbType) {
            case "mysql":
                return "`" + tableName + "`";
            case "postgresql":
                return "\"" + tableName + "\"";
            case "sqlserver":
                return "[" + tableName + "]";
            case "oracle":
                return "\"" + tableName + "\"";
            default:
                return "`" + tableName + "`";
        }
    }
    
    // 辅助方法：转义列名
    private String escapeColumnName(String columnName, String dbType) {
        switch (dbType) {
            case "mysql":
                return "`" + columnName + "`";
            case "postgresql":
                return "\"" + columnName + "\"";
            case "sqlserver":
                return "[" + columnName + "]";
            case "oracle":
                return "\"" + columnName + "\"";
            default:
                return "`" + columnName + "`";
        }
    }
    
    // 辅助方法：转义索引名
    private String escapeIndexName(String indexName, String dbType) {
        switch (dbType) {
            case "mysql":
                return "`" + indexName + "`";
            case "postgresql":
                return "\"" + indexName + "\"";
            case "sqlserver":
                return "[" + indexName + "]";
            case "oracle":
                return "\"" + indexName + "\"";
            default:
                return "`" + indexName + "`";
        }
    }
    
    // 辅助方法：转义外键名
    private String escapeForeignKeyName(String fkName, String dbType) {
        switch (dbType) {
            case "mysql":
                return "`" + fkName + "`";
            case "postgresql":
                return "\"" + fkName + "\"";
            case "sqlserver":
                return "[" + fkName + "]";
            case "oracle":
                return "\"" + fkName + "\"";
            default:
                return "`" + fkName + "`";
        }
    }
    
    // 辅助方法：转义 SQL 字符串
    private String escapeSqlString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "''");
    }
}
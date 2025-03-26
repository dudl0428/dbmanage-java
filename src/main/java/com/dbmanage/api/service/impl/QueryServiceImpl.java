package com.dbmanage.api.service.impl;

import com.dbmanage.api.dto.query.QueryHistoryResponse;
import com.dbmanage.api.dto.query.QueryRequest;
import com.dbmanage.api.dto.query.QueryResponse;
import com.dbmanage.api.dto.query.SavedQueryResponse;
import com.dbmanage.api.exception.ResourceNotFoundException;
import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.QueryHistory;
import com.dbmanage.api.model.SavedQuery;
import com.dbmanage.api.model.User;
import com.dbmanage.api.repository.DatabaseConnectionRepository;
import com.dbmanage.api.repository.QueryHistoryRepository;
import com.dbmanage.api.repository.SavedQueryRepository;
import com.dbmanage.api.repository.UserRepository;
import com.dbmanage.api.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查询服务实现类
 */
@Service
public class QueryServiceImpl implements QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
    
    @Autowired
    private DatabaseConnectionRepository connectionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QueryHistoryRepository queryHistoryRepository;
    
    @Autowired
    private SavedQueryRepository savedQueryRepository;
    
    /**
     * 执行SQL查询
     * @param request 查询请求
     * @return 查询响应
     */
    @Override
    @Transactional
    public QueryResponse executeQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 创建响应对象
        QueryResponse response = new QueryResponse();
        response.setSuccess(false);
        
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(request.getConnectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + request.getConnectionId()));
        
        // 执行查询
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword());
             Statement stmt = conn.createStatement()) {
            
            // 检查SQL类型
            String sql = request.getSql().trim();
            
            // 设置当前数据库上下文
            String database = request.getDatabase();
            if (database != null && !database.isEmpty()) {
                // 执行USE语句切换到指定数据库
                stmt.execute("USE `" + database + "`");
                logger.info("切换到数据库: {}", database);
            }
            
            // 检查是否有多个SQL语句（通过分号分隔）
            String[] sqlStatements = sql.split(";");
            
            // 默认使用最后一个语句的结果作为返回
            boolean isQueryResult = false;
            boolean lastStatementSuccess = false;
            
            for (int i = 0; i < sqlStatements.length; i++) {
                String currentSql = sqlStatements[i].trim();
                if (currentSql.isEmpty()) {
                    continue; // 跳过空语句
                }
                
                logger.info("执行SQL语句 {}/{}: {}", i + 1, sqlStatements.length, currentSql);
                
                boolean isCurrentQueryResult = isSelectQuery(currentSql);
                
                // 如果是最后一个有效语句，记录查询类型
                if (i == sqlStatements.length - 1 || i == sqlStatements.length - 2 && sqlStatements[sqlStatements.length - 1].trim().isEmpty()) {
                    isQueryResult = isCurrentQueryResult;
                }
                
                if (isCurrentQueryResult) {
                    try (ResultSet rs = stmt.executeQuery(currentSql)) {
                        // 如果是最后一个语句，或者是包含有意义结果的查询，获取结果
                        if (i == sqlStatements.length - 1 || i == sqlStatements.length - 2 && sqlStatements[sqlStatements.length - 1].trim().isEmpty()) {
                            ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();
                            
                            // 获取列名
                            List<String> columns = new ArrayList<>();
                            for (int j = 1; j <= columnCount; j++) {
                                columns.add(metaData.getColumnLabel(j));
                            }
                            response.setColumns(columns);
                            
                            // 获取数据
                            List<Map<String, Object>> data = new ArrayList<>();
                            while (rs.next()) {
                                Map<String, Object> row = new HashMap<>();
                                for (int j = 1; j <= columnCount; j++) {
                                    String columnName = metaData.getColumnLabel(j);
                                    Object value = rs.getObject(j);
                                    row.put(columnName, value);
                                }
                                data.add(row);
                            }
                            response.setData(data);
                        }
                        lastStatementSuccess = true;
                    }
                } else {
                    // 非SELECT查询（如INSERT, UPDATE, DELETE）
                    int affectedRows = stmt.executeUpdate(currentSql);
                    // 如果是最后一个语句，记录影响的行数
                    if (i == sqlStatements.length - 1 || i == sqlStatements.length - 2 && sqlStatements[sqlStatements.length - 1].trim().isEmpty()) {
                        response.setAffectedRows(affectedRows);
                    }
                    lastStatementSuccess = true;
                }
            }
            
            // 设置查询执行是否成功
            response.setSuccess(lastStatementSuccess);
            response.setQueryResult(isQueryResult);
            
            // 保存查询历史
            saveQueryHistory(connection, request.getSql(), System.currentTimeMillis() - startTime, response, null);
            
        } catch (SQLException e) {
            logger.error("SQL execution error: ", e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
            
            // 保存错误的查询历史
            saveQueryHistory(connection, request.getSql(), System.currentTimeMillis() - startTime, response, e.getMessage());
        }
        
        // 设置执行时间
        response.setExecutionTime(System.currentTimeMillis() - startTime);
        
        return response;
    }
    
    /**
     * 获取数据库结构信息
     * @param connectionId 数据库连接ID
     * @return 数据库结构信息
     */
    @Override
    public List<Map<String, Object>> getDatabaseStructure(Long connectionId) {
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword())) {
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取catalog列表
            List<Map<String, Object>> catalogs = new ArrayList<>();
            try (ResultSet rs = metaData.getCatalogs()) {
                while (rs.next()) {
                    String catalogName = rs.getString("TABLE_CAT");
                    
                    // 构建catalog节点
                    Map<String, Object> catalogNode = new HashMap<>();
                    catalogNode.put("id", "catalog-" + catalogName);
                    catalogNode.put("label", catalogName);
                    catalogNode.put("type", "database");
                    
                    // 获取schema列表
                    List<Map<String, Object>> schemas = new ArrayList<>();
                    try (ResultSet schemaRs = metaData.getSchemas(catalogName, null)) {
                        while (schemaRs.next()) {
                            String schemaName = schemaRs.getString("TABLE_SCHEM");
                            
                            // 构建schema节点
                            Map<String, Object> schemaNode = new HashMap<>();
                            schemaNode.put("id", "schema-" + catalogName + "-" + schemaName);
                            schemaNode.put("label", schemaName);
                            schemaNode.put("type", "schema");
                            
                            // 获取表列表
                            List<Map<String, Object>> tables = new ArrayList<>();
                            try (ResultSet tableRs = metaData.getTables(catalogName, schemaName, null, new String[]{"TABLE"})) {
                                while (tableRs.next()) {
                                    String tableName = tableRs.getString("TABLE_NAME");
                                    
                                    // 构建表节点
                                    Map<String, Object> tableNode = new HashMap<>();
                                    tableNode.put("id", "table-" + catalogName + "-" + schemaName + "-" + tableName);
                                    tableNode.put("label", tableName);
                                    tableNode.put("type", "table");
                                    
                                    // 获取列列表
                                    List<Map<String, Object>> columns = new ArrayList<>();
                                    try (ResultSet columnRs = metaData.getColumns(catalogName, schemaName, tableName, null)) {
                                        while (columnRs.next()) {
                                            String columnName = columnRs.getString("COLUMN_NAME");
                                            String columnType = columnRs.getString("TYPE_NAME");
                                            
                                            // 构建列节点
                                            Map<String, Object> columnNode = new HashMap<>();
                                            columnNode.put("id", "column-" + catalogName + "-" + schemaName + "-" + tableName + "-" + columnName);
                                            columnNode.put("label", columnName + " (" + columnType + ")");
                                            columnNode.put("type", "column");
                                            
                                            columns.add(columnNode);
                                        }
                                    }
                                    
                                    // 添加列到表节点
                                    tableNode.put("children", columns);
                                    tables.add(tableNode);
                                }
                            }
                            
                            // 添加表到schema节点
                            schemaNode.put("children", tables);
                            schemas.add(schemaNode);
                        }
                    }
                    
                    // 添加schema到catalog节点
                    catalogNode.put("children", schemas);
                    catalogs.add(catalogNode);
                }
            }
            
            result = catalogs;
            
        } catch (SQLException e) {
            logger.error("Error fetching database structure: ", e);
            throw new RuntimeException("Error fetching database structure: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取表结构信息
     * @param connectionId 数据库连接ID
     * @param tableName 表名
     * @return 表结构信息
     */
    @Override
    public List<Map<String, Object>> getTableStructure(Long connectionId, String tableName) {
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword())) {
            
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = null;
            String schema = null;
            String table = tableName;
            
            // 处理可能包含数据库名的表名
            if (tableName.contains(".")) {
                String[] parts = tableName.split("\\.");
                if (parts.length > 1) {
                    catalog = parts[0];
                    table = parts[1];
                }
            }
            
            // 获取表注释信息
            String tableComment = "";
            try {
                // MySQL获取表注释
                if ("mysql".equalsIgnoreCase(connection.getType())) {
                    try (Statement stmt = conn.createStatement()) {
                        String sql = "SELECT TABLE_COMMENT FROM information_schema.TABLES " +
                                "WHERE TABLE_SCHEMA = '" + (catalog != null ? catalog : conn.getCatalog()) + "' " +
                                "AND TABLE_NAME = '" + table + "'";
                        try (ResultSet rs = stmt.executeQuery(sql)) {
                            if (rs.next()) {
                                tableComment = rs.getString("TABLE_COMMENT");
                            }
                        }
                    }
                } 
                // PostgreSQL获取表注释
                else if ("postgresql".equalsIgnoreCase(connection.getType())) {
                    try (Statement stmt = conn.createStatement()) {
                        String sql = "SELECT obj_description('" + 
                                (schema != null ? schema : "public") + "." + table + 
                                "'::regclass, 'pg_class') AS comment";
                        try (ResultSet rs = stmt.executeQuery(sql)) {
                            if (rs.next()) {
                                tableComment = rs.getString("comment");
                            }
                        }
                    }
                }
                // 其他数据库类型添加相应的注释获取逻辑
            } catch (Exception e) {
                logger.warn("Error getting table comment for {}: {}", tableName, e.getMessage());
                // 不中断处理，继续获取字段信息
            }
            
            // 存储表信息作为元数据
            Map<String, Object> tableInfo = new HashMap<>();
            tableInfo.put("name", table);
            tableInfo.put("comment", tableComment != null ? tableComment : "");
            tableInfo.put("catalogName", catalog);
            tableInfo.put("schemaName", schema);
            tableInfo.put("fullName", tableName);
            tableInfo.put("type", "TABLE");
            result.add(tableInfo);
            
            // 获取表的主键信息
            Set<String> primaryKeys = new HashSet<>();
            try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, table)) {
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            }
            
            // 获取表字段结构信息
            try (ResultSet rs = metaData.getColumns(catalog, schema, table, null)) {
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    String columnName = rs.getString("COLUMN_NAME");
                    
                    column.put("name", columnName);
                    column.put("type", rs.getString("TYPE_NAME"));
                    column.put("size", rs.getInt("COLUMN_SIZE"));
                    column.put("nullable", rs.getString("IS_NULLABLE").equals("YES"));
                    column.put("defaultValue", rs.getString("COLUMN_DEF"));
                    column.put("ordinalPosition", rs.getInt("ORDINAL_POSITION"));
                    column.put("isPrimaryKey", primaryKeys.contains(columnName));
                    
                    // 获取列注释
                    String columnComment = "";
                    try {
                        // MySQL获取列注释
                        if ("mysql".equalsIgnoreCase(connection.getType())) {
                            try (Statement stmt = conn.createStatement()) {
                                String sql = "SELECT COLUMN_COMMENT FROM information_schema.COLUMNS " +
                                        "WHERE TABLE_SCHEMA = '" + (catalog != null ? catalog : conn.getCatalog()) + "' " +
                                        "AND TABLE_NAME = '" + table + "' " +
                                        "AND COLUMN_NAME = '" + columnName + "'";
                                try (ResultSet commentRs = stmt.executeQuery(sql)) {
                                    if (commentRs.next()) {
                                        columnComment = commentRs.getString("COLUMN_COMMENT");
                                    }
                                }
                            }
                        } 
                        // PostgreSQL获取列注释
                        else if ("postgresql".equalsIgnoreCase(connection.getType())) {
                            try (Statement stmt = conn.createStatement()) {
                                String sql = "SELECT col_description('" + 
                                        (schema != null ? schema : "public") + "." + table + 
                                        "'::regclass::oid, " + rs.getInt("ORDINAL_POSITION") + ") AS comment";
                                try (ResultSet commentRs = stmt.executeQuery(sql)) {
                                    if (commentRs.next()) {
                                        columnComment = commentRs.getString("comment");
                                    }
                                }
                            }
                        }
                        // 其他数据库类型可以添加相应的注释获取逻辑
                    } catch (Exception e) {
                        logger.warn("Error getting column comment for {}.{}: {}", tableName, columnName, e.getMessage());
                        // 不中断处理，继续获取其他字段
                    }
                    
                    column.put("comment", columnComment != null ? columnComment : "");
                    result.add(column);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching table structure: ", e);
            throw new RuntimeException("Error fetching table structure: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取数据库表列表
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 表列表信息
     */
    @Override
    public List<Map<String, Object>> getDatabaseTables(Long connectionId, String database, String schemaName) {
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword())) {
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取表列表
            try (ResultSet rs = metaData.getTables(database, schemaName, null, new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");
                    String remarks = rs.getString("REMARKS");
                    
                    Map<String, Object> table = new HashMap<>();
                    table.put("id", "table-" + database + "-" + schemaName + "-" + tableName);
                    table.put("label", tableName);
                    table.put("type", "table");
                    table.put("tableType", tableType);
                    table.put("remarks", remarks);
                    
                    result.add(table);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching database tables: ", e);
            throw new RuntimeException("Error fetching database tables: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取数据库视图列表
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 视图列表信息
     */
    @Override
    public List<Map<String, Object>> getDatabaseViews(Long connectionId, String database, String schemaName) {
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword())) {
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取视图列表
            try (ResultSet rs = metaData.getTables(database, schemaName, null, new String[]{"VIEW"})) {
                while (rs.next()) {
                    String viewName = rs.getString("TABLE_NAME");
                    String remarks = rs.getString("REMARKS");
                    
                    Map<String, Object> view = new HashMap<>();
                    view.put("id", "view-" + database + "-" + schemaName + "-" + viewName);
                    view.put("label", viewName);
                    view.put("type", "view");
                    view.put("remarks", remarks);
                    
                    result.add(view);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error fetching database views: ", e);
            throw new RuntimeException("Error fetching database views: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取数据库函数列表
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 函数列表信息
     */
    @Override
    public List<Map<String, Object>> getDatabaseFunctions(Long connectionId, String database, String schemaName) {
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword())) {
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 由于JDBC API没有直接支持获取函数列表的方法，根据不同数据库类型需要不同处理
            // 以MySQL为例
            if ("mysql".equalsIgnoreCase(connection.getType())) {
                try (Statement stmt = conn.createStatement()) {
                    // 查询MySQL数据库函数
                    String sql = "SELECT routine_name FROM information_schema.routines " +
                                 "WHERE routine_schema = '" + database + "' AND routine_type = 'FUNCTION'";
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            String functionName = rs.getString("routine_name");
                            
                            Map<String, Object> function = new HashMap<>();
                            function.put("id", "function-" + database + "-" + (schemaName != null ? schemaName + "-" : "") + functionName);
                            function.put("label", functionName);
                            function.put("type", "function");
                            
                            result.add(function);
                        }
                    }
                }
            } else if ("postgresql".equalsIgnoreCase(connection.getType())) {
                // PostgreSQL处理
                try (Statement stmt = conn.createStatement()) {
                    String sql = "SELECT proname FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid " +
                                 "WHERE n.nspname = '" + (schemaName != null ? schemaName : "public") + "'";
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            String functionName = rs.getString("proname");
                            
                            Map<String, Object> function = new HashMap<>();
                            function.put("id", "function-" + database + "-" + (schemaName != null ? schemaName + "-" : "") + functionName);
                            function.put("label", functionName);
                            function.put("type", "function");
                            
                            result.add(function);
                        }
                    }
                }
            }
            // 其他数据库类型的处理可以根据需要添加
            
        } catch (SQLException e) {
            logger.error("Error fetching database functions: ", e);
            throw new RuntimeException("Error fetching database functions: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 获取数据库事件列表
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param schemaName 模式名称
     * @return 事件列表信息
     */
    @Override
    public List<Map<String, Object>> getDatabaseEvents(Long connectionId, String database, String schemaName) {
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword())) {
            
            // 由于JDBC API没有直接支持获取事件列表的方法，根据不同数据库类型需要不同处理
            // 以MySQL为例，MySQL支持事件
            if ("mysql".equalsIgnoreCase(connection.getType())) {
                try (Statement stmt = conn.createStatement()) {
                    // 查询MySQL数据库事件
                    String sql = "SELECT event_name FROM information_schema.events " +
                                 "WHERE event_schema = '" + database + "'";
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            String eventName = rs.getString("event_name");
                            
                            Map<String, Object> event = new HashMap<>();
                            event.put("id", "event-" + database + "-" + (schemaName != null ? schemaName + "-" : "") + eventName);
                            event.put("label", eventName);
                            event.put("type", "event");
                            
                            result.add(event);
                        }
                    }
                }
            }
            // 其他数据库类型可能不支持事件，或有不同的实现
            
        } catch (SQLException e) {
            logger.error("Error fetching database events: ", e);
            throw new RuntimeException("Error fetching database events: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 保存查询
     * @param name 查询名称
     * @param description 查询描述
     * @param sql SQL语句
     * @param connectionId 数据库连接ID
     * @param userId 用户ID
     * @return 保存结果
     */
    @Override
    @Transactional
    public boolean saveQuery(String name, String description, String sql, Long connectionId, Long userId) {
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        try {
            // 创建保存的查询对象
            SavedQuery savedQuery = new SavedQuery();
            savedQuery.setName(name);
            savedQuery.setDescription(description);
            savedQuery.setSql(sql);
            savedQuery.setConnection(connection);
            savedQuery.setUser(user);
            
            // 保存查询
            savedQueryRepository.save(savedQuery);
            
            return true;
        } catch (Exception e) {
            logger.error("Error saving query: ", e);
            return false;
        }
    }
    
    /**
     * 获取用户保存的查询列表
     * @param userId 用户ID
     * @return 查询列表
     */
    @Override
    public List<SavedQueryResponse> getSavedQueries(Long userId) {
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        // 获取保存的查询列表
        List<SavedQuery> savedQueries = savedQueryRepository.findByUser(user);
        
        // 转换为Response对象列表
        return savedQueries.stream()
                .map(this::convertToSavedQueryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户查询历史
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 查询历史列表
     */
    @Override
    public List<QueryHistoryResponse> getQueryHistory(Long userId, int limit) {
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        // 获取查询历史列表
        List<QueryHistory> queryHistories = queryHistoryRepository.findByUserOrderByExecutedAtDesc(user, PageRequest.of(0, limit)).getContent();
        
        // 转换为Response对象列表
        return queryHistories.stream()
                .map(this::convertToQueryHistoryResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 执行SQL更新操作（INSERT、UPDATE、DELETE等）
     * @param connectionId 数据库连接ID
     * @param sql SQL语句
     * @param params SQL参数
     * @return 更新结果，包含影响的行数等信息
     */
    @Override
    @Transactional
    public Map<String, Object> executeUpdate(Long connectionId, String sql, Object[] params) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        // 查找连接
        DatabaseConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
        
        try (Connection conn = DriverManager.getConnection(
                connection.getUrl(),
                connection.getUsername(),
                connection.getPassword());
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
            }
            
            // 执行更新
            int affectedRows = pstmt.executeUpdate();
            
            // 获取生成的主键（如果有）
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            List<Object> generatedIds = new ArrayList<>();
            if (generatedKeys != null) {
                while (generatedKeys.next()) {
                    generatedIds.add(generatedKeys.getObject(1));
                }
            }
            
            // 设置结果
            result.put("success", true);
            result.put("affectedRows", affectedRows);
            result.put("executionTime", System.currentTimeMillis() - startTime);
            if (!generatedIds.isEmpty()) {
                result.put("generatedKeys", generatedIds);
            }
            
            // 保存查询历史
            saveQueryHistory(connection, sql, System.currentTimeMillis() - startTime, 
                new QueryResponse(true, false, affectedRows), null);
            
        } catch (SQLException e) {
            logger.error("SQL execution error: ", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("executionTime", System.currentTimeMillis() - startTime);
            
            // 保存错误的查询历史
            saveQueryHistory(connection, sql, System.currentTimeMillis() - startTime, 
                new QueryResponse(false, false, 0), e.getMessage());
        }
        
        return result;
    }
    
    // 辅助方法：判断SQL是否为SELECT查询
    private boolean isSelectQuery(String sql) {
        return sql.trim().toLowerCase().startsWith("select");
    }
    
    // 辅助方法：保存查询历史
    private void saveQueryHistory(DatabaseConnection connection, String sql, long executionTime, QueryResponse response, String errorMessage) {
        try {
            User user = connection.getUser();
            
            QueryHistory history = new QueryHistory();
            history.setSql(sql);
            history.setQueryText(sql);
            history.setExecutionTime(executionTime);
            history.setIsSuccess(response.isSuccess());
            
            if (response.isSuccess()) {
                if (response.isQueryResult()) {
                    // SELECT查询
                    history.setAffectedRows(0);
                } else {
                    // 非SELECT查询
                    history.setAffectedRows(response.getAffectedRows());
                }
            } else {
                history.setErrorMessage(errorMessage);
            }
            
            history.setConnection(connection);
            history.setUser(user);
            
            queryHistoryRepository.save(history);
        } catch (Exception e) {
            logger.error("Error saving query history: ", e);
        }
    }
    
    // 辅助方法：将SavedQuery转换为SavedQueryResponse
    private SavedQueryResponse convertToSavedQueryResponse(SavedQuery savedQuery) {
        SavedQueryResponse response = new SavedQueryResponse();
        response.setId(savedQuery.getId());
        response.setName(savedQuery.getName());
        response.setDescription(savedQuery.getDescription());
        response.setSql(savedQuery.getSql());
        response.setConnectionId(savedQuery.getConnection().getId());
        response.setConnectionName(savedQuery.getConnection().getName());
        response.settype(savedQuery.getConnection().getType());
        
        // 转换Date为LocalDateTime
        if (savedQuery.getCreatedAt() != null) {
            response.setCreatedAt(LocalDateTime.ofInstant(
                savedQuery.getCreatedAt().toInstant(), ZoneId.systemDefault()));
        }
        
        if (savedQuery.getUpdatedAt() != null) {
            response.setUpdatedAt(LocalDateTime.ofInstant(
                savedQuery.getUpdatedAt().toInstant(), ZoneId.systemDefault()));
        }
        
        return response;
    }
    
    // 辅助方法：将QueryHistory转换为QueryHistoryResponse
    private QueryHistoryResponse convertToQueryHistoryResponse(QueryHistory queryHistory) {
        QueryHistoryResponse response = new QueryHistoryResponse();
        response.setId(queryHistory.getId());
        response.setSql(queryHistory.getSql());
        response.setExecutionTime(queryHistory.getExecutionTime());
        response.setAffectedRows(queryHistory.getAffectedRows());
        response.setSuccess(queryHistory.getIsSuccess());
        response.setErrorMessage(queryHistory.getErrorMessage());
        response.setConnectionId(queryHistory.getConnection().getId());
        response.setConnectionName(queryHistory.getConnection().getName());
        response.settype(queryHistory.getConnection().getType());
        
        // 转换Date为LocalDateTime
        if (queryHistory.getExecutedAt() != null) {
            response.setExecutedAt(LocalDateTime.ofInstant(
                queryHistory.getExecutedAt().toInstant(), ZoneId.systemDefault()));
        }
        
        return response;
    }

    /**
     * 执行带分页的SQL查询
     * 
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param sql SQL语句
     * @param offset 偏移量
     * @param limit 每页记录数
     * @return 查询结果，包含分页信息
     */
    @Override
    public Map<String, Object> executeQueryWithPagination(Long connectionId, String database, String sql, int offset, int limit) {
        // 创建QueryRequest
        QueryRequest request = new QueryRequest();
        request.setConnectionId(connectionId);
        request.setDatabase(database);
        request.setSql(sql);
        
        try {
            // 执行查询
            QueryResponse response = executeQuery(request);
            
            // 计算总记录数 - 这里简化处理，实际应该通过COUNT查询获取
            long totalRecords = response.getData() != null ? response.getData().size() : 0;
            
            // 构建分页信息
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("total", totalRecords);
            pagination.put("current", offset / limit + 1);
            pagination.put("pageSize", limit);
            pagination.put("totalPages", (totalRecords + limit - 1) / limit);
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("pagination", pagination);
            result.put("columns", response.getColumns());
            result.put("data", response.getData());
            
            return result;
        } catch (Exception e) {
            logger.error("执行分页查询失败", e);
            
            // 返回错误信息
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("success", false);
            
            return errorResult;
        }
    }

    /**
     * 执行一般SQL查询
     * 
     * @param connectionId 数据库连接ID
     * @param database 数据库名称
     * @param sql SQL语句
     * @return 查询结果
     */
    @Override
    public Map<String, Object> executeGenericQuery(Long connectionId, String database, String sql) {
        // 创建QueryRequest
        QueryRequest request = new QueryRequest();
        request.setConnectionId(connectionId);
        request.setDatabase(database);
        request.setSql(sql);
        
        try {
            // 执行查询
            QueryResponse response = executeQuery(request);
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("message", response.getErrorMessage());
            result.put("isQueryResult", response.isQueryResult());
            result.put("affectedRows", response.getAffectedRows());
            result.put("columns", response.getColumns());
            result.put("data", response.getData());
            result.put("executionTime", response.getExecutionTime());
            
            return result;
        } catch (Exception e) {
            logger.error("执行查询失败", e);
            
            // 返回错误信息
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("success", false);
            
            return errorResult;
        }
    }
} 
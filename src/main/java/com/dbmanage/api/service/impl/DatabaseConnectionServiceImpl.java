package com.dbmanage.api.service.impl;

import com.dbmanage.api.controller.ConnectionGroupController;
import com.dbmanage.api.dto.connection.ConnectionGroupResponse;
import com.dbmanage.api.dto.connection.ConnectionRequest;
import com.dbmanage.api.dto.connection.ConnectionResponse;
import com.dbmanage.api.dto.connection.ConnectionTestRequest;
import com.dbmanage.api.exception.ResourceNotFoundException;
import com.dbmanage.api.model.ConnectionGroup;
import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.User;
import com.dbmanage.api.repository.DatabaseConnectionRepository;
import com.dbmanage.api.repository.UserRepository;
import com.dbmanage.api.service.ConnectionGroupService;
import com.dbmanage.api.service.ConnectionService;
import com.dbmanage.api.service.DatabaseConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据库连接服务实现类
 */
@Service
public class DatabaseConnectionServiceImpl implements DatabaseConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionServiceImpl.class);
    
    @Autowired
    private DatabaseConnectionRepository connectionRepository;
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConnectionGroupService connectionGroupService;
    @Autowired
    private ConnectionService connectionService;
    
    /**
     * 创建数据库连接
     * @param userId 用户ID
     * @param request 连接请求
     * @return 连接响应
     */
    @Override
    @Transactional
    public ConnectionResponse createConnection(Long userId, ConnectionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        if (connectionRepository.existsByUserAndName(user, request.getName())) {
            throw new IllegalArgumentException("Connection with name " + request.getName() + " already exists");
        }
        
        DatabaseConnection connection = new DatabaseConnection();
        connection.setName(request.getName());
        connection.setType(request.getType());
        connection.setHost(request.getHost());
        connection.setPort(request.getPort());
        connection.setDatabase(request.getDatabase());
        connection.setUsername(request.getUsername());
        connection.setPassword(request.getPassword());
        if(Objects.nonNull(request.getGroupId())){
            ConnectionGroupResponse group = connectionGroupService.getGroup(userId, request.getGroupId(), false);
            ConnectionGroup connectionGroup= new ConnectionGroup();
            connectionGroup.setId(group.getId());
            connectionGroup.setName(group.getName());
            connectionGroup.setUser(user);
            connectionGroup.setConnections(new HashSet<>());
           connection.setGroup(connectionGroup);
        }
        connection.setPassword(request.getPassword());
        connection.setParameters(request.getParameters());
        connection.setUser(user);
        
        // 生成JDBC URL
        connection.setUrl(generateJdbcUrl(connection));
        
        DatabaseConnection savedConnection = connectionRepository.save(connection);
        
        return new ConnectionResponse(savedConnection);
    }
    
    /**
     * 更新数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @param request 连接请求
     * @return 连接响应
     */
    @Override
    @Transactional
    public ConnectionResponse updateConnection(Long userId, Long connectionId, ConnectionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        DatabaseConnection connection = connectionRepository.findByIdAndUser(connectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
        
        // 如果名称已更改，检查新名称是否已存在
        if (!connection.getName().equals(request.getName()) && 
                connectionRepository.existsByUserAndName(user, request.getName())) {
            throw new IllegalArgumentException("Connection with name " + request.getName() + " already exists");
        }
        
        connection.setName(request.getName());
        connection.setType(request.getType());
        connection.setHost(request.getHost());
        connection.setPort(request.getPort());
        connection.setDatabase(request.getDatabase());
        connection.setUsername(request.getUsername());
        
        // 仅在密码不为空时更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            connection.setPassword(request.getPassword());
        }
        
        connection.setParameters(request.getParameters());
        
        // 更新JDBC URL
        connection.setUrl(generateJdbcUrl(connection));
        
        DatabaseConnection updatedConnection = connectionRepository.save(connection);
        
        return new ConnectionResponse(updatedConnection);
    }
    
    /**
     * 获取数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @return 连接响应
     */
    @Override
    public ConnectionResponse getConnection(Long userId, Long connectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        DatabaseConnection connection = connectionRepository.findByIdAndUser(connectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
                
        return new ConnectionResponse(connection);
    }
    
    /**
     * 获取用户的所有数据库连接
     * @param userId 用户ID
     * @return 连接响应列表
     */
    @Override
    public List<ConnectionResponse> getUserConnections(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        return connectionRepository.findByUser(user).stream()
                .map(ConnectionResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean deleteConnection(Long userId, Long connectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        DatabaseConnection connection = connectionRepository.findByIdAndUser(connectionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
        
        connectionRepository.delete(connection);
        return true;
    }
    
    /**
     * 测试数据库连接
     * @param request 连接测试请求
     * @return 测试结果
     */
    @Override
    public Map<String, Object> testConnection(ConnectionTestRequest request) {
        Map<String, Object> result = new HashMap<>();
        Connection conn = null;
        
        try {
            // 构建临时连接对象用于生成URL
            DatabaseConnection tempConnection = new DatabaseConnection();
            tempConnection.setType(request.getType());
            tempConnection.setHost(request.getHost());
            tempConnection.setPort(request.getPort());
            tempConnection.setDatabase(request.getDatabase());
            tempConnection.setParameters(request.getParameters());
            
            String jdbcUrl = generateJdbcUrl(tempConnection);
            
            // 注册驱动
            registerJdbcDriver(request.getType());
            
            // 尝试建立连接
            conn = DriverManager.getConnection(jdbcUrl, request.getUsername(), request.getPassword());
            
            result.put("success", true);
            result.put("message", "Connection successful");
            result.put("url", jdbcUrl);
            
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found: ", e);
            result.put("success", false);
            result.put("message", "Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            logger.error("Connection failed: ", e);
            result.put("success", false);
            result.put("message", "Connection failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: ", e);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 根据连接ID打开数据库连接
     * @param userId 用户ID
     * @param connectionId 连接ID
     * @return 连接结果
     */
    @Override
    public Map<String, Object> openConnection(Long userId, Long connectionId) {
        Map<String, Object> result = new HashMap<>();
        Connection conn = null;
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    
            DatabaseConnection connection = connectionRepository.findByIdAndUser(connectionId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Connection not found with id: " + connectionId));
            
            // 注册驱动
            registerJdbcDriver(connection.getType());
            
            // 尝试建立连接
            conn = DriverManager.getConnection(
                    connection.getUrl(),
                    connection.getUsername(), 
                    connection.getPassword()
            );
            connectionService.cacheConnection(connectionId, connection);
            // 更新最后连接时间
            connection.setLastConnected(new Date());
            connectionRepository.save(connection);

            result.put("success", true);
            result.put("message", "Connection opened successfully");
            result.put("connection", new ConnectionResponse(connection));
        } catch (ClassNotFoundException e) {
            logger.error("Driver not found: ", e);
            result.put("success", false);
            result.put("message", "Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            logger.error("Connection failed: ", e);
            result.put("success", false);
            result.put("message", "Connection failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: ", e);
                }
            }
        }
        
        return result;
    }

    @Override
    public void cacheConnection(Long id) {
        connectionService.removeConnection(id);
    }

    /**
     * 生成JDBC URL
     * @param connection 数据库连接
     * @return JDBC URL
     */
    private String generateJdbcUrl(DatabaseConnection connection) {
        StringBuilder urlBuilder = new StringBuilder("jdbc:");
        
        switch (connection.getType().toLowerCase()) {
            case "mysql":
                urlBuilder.append("mysql://")
                        .append(connection.getHost())
                        .append(":")
                        .append(connection.getPort() != null ? connection.getPort() : 3306)
                        .append("/")
                        .append(connection.getDatabase());
                break;
            case "postgresql":
                urlBuilder.append("postgresql://")
                        .append(connection.getHost())
                        .append(":")
                        .append(connection.getPort() != null ? connection.getPort() : 5432)
                        .append("/")
                        .append(connection.getDatabase());
                break;
            case "oracle":
                urlBuilder.append("oracle:thin:@")
                        .append(connection.getHost())
                        .append(":")
                        .append(connection.getPort() != null ? connection.getPort() : 1521)
                        .append(":")
                        .append(connection.getDatabase());
                break;
            case "sqlserver":
                urlBuilder.append("sqlserver://")
                        .append(connection.getHost())
                        .append(":")
                        .append(connection.getPort() != null ? connection.getPort() : 1433)
                        .append(";database=")
                        .append(connection.getDatabase());
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + connection.getType());
        }
        
        // 添加其他参数
        if (connection.getParameters() != null && !connection.getParameters().isEmpty()) {
            if (connection.getType().equalsIgnoreCase("oracle")) {
                // Oracle使用不同的参数格式
                urlBuilder.append("?").append(connection.getParameters());
            } else {
                // MySQL, PostgreSQL, SQL Server使用相似的参数格式
                urlBuilder.append(connection.getType().equalsIgnoreCase("sqlserver") ? ";" : "?")
                        .append(connection.getParameters());
            }
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * 注册JDBC驱动
     * @param dbType 数据库类型
     * @throws ClassNotFoundException 如果找不到驱动类
     */
    private void registerJdbcDriver(String dbType) throws ClassNotFoundException {
        switch (dbType.toLowerCase()) {
            case "mysql":
                Class.forName("com.mysql.cj.jdbc.Driver");
                break;
            case "postgresql":
                Class.forName("org.postgresql.Driver");
                break;
            case "oracle":
                Class.forName("oracle.jdbc.driver.OracleDriver");
                break;
            case "sqlserver":
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
} 
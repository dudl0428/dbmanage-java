package com.dbmanage.api.repository;

import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 数据库连接仓库接口
 */
@Repository
public interface DatabaseConnectionRepository extends JpaRepository<DatabaseConnection, Long> {

    /**
     * 根据用户查找数据库连接
     * @param user 用户
     * @return 数据库连接列表
     */
    List<DatabaseConnection> findByUser(User user);

    /**
     * 根据用户和连接名称查找数据库连接
     * @param user 用户
     * @param name 连接名称
     * @return 数据库连接
     */
    Optional<DatabaseConnection> findByUserAndName(User user, String name);

    /**
     * 检查指定用户下是否存在指定名称的连接
     * @param user 用户
     * @param name 连接名称
     * @return 是否存在
     */
    boolean existsByUserAndName(User user, String name);

    /**
     * 根据ID和用户查找数据库连接
     * @param id 连接ID
     * @param user 用户
     * @return 数据库连接
     */
    Optional<DatabaseConnection> findByIdAndUser(Long id, User user);

    /**
     * 根据用户和连接类型查找数据库连接
     * @param user 用户
     * @param type 连接类型
     * @return 数据库连接列表
     */
    List<DatabaseConnection> findByUserAndType(User user, String type);
} 
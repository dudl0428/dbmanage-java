package com.dbmanage.api.repository;

import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.TableFavorite;
import com.dbmanage.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 表收藏数据访问接口
 * 提供表收藏相关的数据库操作方法
 */
@Repository
public interface TableFavoriteRepository extends JpaRepository<TableFavorite, Long> {
    
    /**
     * 按表名升序查找用户的所有表收藏
     * @param user 用户对象
     * @return 表收藏列表
     */
    List<TableFavorite> findByUserOrderByTableNameAsc(User user);
    
    /**
     * 按表名升序查找用户在指定数据库连接下的表收藏
     * @param user 用户对象
     * @param connection 数据库连接对象
     * @return 表收藏列表
     */
    List<TableFavorite> findByUserAndConnectionOrderByTableNameAsc(User user, DatabaseConnection connection);
    
    /**
     * 根据数据库连接、用户、表名和模式名查找表收藏
     * @param connection 数据库连接对象
     * @param user 用户对象
     * @param tableName 表名
     * @param schemaName 模式名
     * @return 表收藏对象，如果不存在返回空
     */
    Optional<TableFavorite> findByConnectionAndUserAndTableNameAndSchemaName(DatabaseConnection connection, User user, String tableName, String schemaName);
    
    /**
     * 检查用户是否已收藏指定数据库连接下的指定表
     * @param connection 数据库连接对象
     * @param user 用户对象
     * @param tableName 表名
     * @param schemaName 模式名
     * @return 如果已收藏返回true，否则返回false
     */
    boolean existsByConnectionAndUserAndTableNameAndSchemaName(DatabaseConnection connection, User user, String tableName, String schemaName);
} 
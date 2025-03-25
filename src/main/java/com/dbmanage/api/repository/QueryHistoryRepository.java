package com.dbmanage.api.repository;

import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.QueryHistory;
import com.dbmanage.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 查询历史仓库接口
 */
@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {
    
    /**
     * 分页查找用户的查询历史，按执行时间降序排序
     * @param user 用户对象
     * @param pageable 分页参数
     * @return 查询历史分页对象
     */
    Page<QueryHistory> findByUserOrderByExecutedAtDesc(User user, Pageable pageable);
    
    /**
     * 根据用户和连接ID查找查询历史，按查询时间降序排序
     * @param user 用户
     * @param connectionId 连接ID
     * @param pageable 分页信息
     * @return 查询历史列表
     */
    List<QueryHistory> findByUserAndConnectionIdOrderByExecutedAtDesc(User user, Long connectionId, Pageable pageable);
    
    /**
     * 分页查找用户在指定数据库连接下的查询历史，按执行时间降序排序
     * @param user 用户对象
     * @param connection 数据库连接对象
     * @param pageable 分页参数
     * @return 查询历史分页对象
     */
    Page<QueryHistory> findByUserAndConnectionOrderByExecutedAtDesc(User user, DatabaseConnection connection, Pageable pageable);
    
    /**
     * 根据ID和用户查找查询历史
     * @param id 查询历史ID
     * @param user 用户
     * @return 查询历史
     */
    QueryHistory findByIdAndUser(Long id, User user);
    
    /**
     * 查找用户收藏的查询历史，按执行时间降序排序
     * @param user 用户对象
     * @return 收藏的查询历史列表
     */
    List<QueryHistory> findByUserAndFavoriteIsTrueOrderByExecutedAtDesc(User user);
    
    /**
     * 查找用户在指定数据库连接下收藏的查询历史，按执行时间降序排序
     * @param user 用户对象
     * @param connection 数据库连接对象
     * @return 收藏的查询历史列表
     */
    List<QueryHistory> findByUserAndConnectionAndFavoriteIsTrueOrderByExecutedAtDesc(User user, DatabaseConnection connection);
} 
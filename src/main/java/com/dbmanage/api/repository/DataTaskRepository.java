package com.dbmanage.api.repository;

import com.dbmanage.api.model.DataTask;
import com.dbmanage.api.model.DatabaseConnection;
import com.dbmanage.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据任务数据访问接口
 * 提供数据导入导出任务相关的数据库操作方法
 */
@Repository
public interface DataTaskRepository extends JpaRepository<DataTask, Long> {
    
    /**
     * 分页查找用户的所有数据任务，按创建时间降序排序
     * @param user 用户对象
     * @param pageable 分页参数
     * @return 数据任务分页对象
     */
    Page<DataTask> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * 分页查找用户在指定数据库连接下的数据任务，按创建时间降序排序
     * @param user 用户对象
     * @param connection 数据库连接对象
     * @param pageable 分页参数
     * @return 数据任务分页对象
     */
    Page<DataTask> findByUserAndConnectionOrderByCreatedAtDesc(User user, DatabaseConnection connection, Pageable pageable);
    
    /**
     * 查找指定状态列表中的所有数据任务
     * @param statuses 状态列表
     * @return 数据任务列表
     */
    List<DataTask> findByStatusIn(List<String> statuses);
    
    /**
     * 查找用户指定状态列表中的所有数据任务
     * @param user 用户对象
     * @param statuses 状态列表
     * @return 数据任务列表
     */
    List<DataTask> findByUserAndStatusIn(User user, List<String> statuses);
} 
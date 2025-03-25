package com.dbmanage.api.repository;

import com.dbmanage.api.model.SavedQuery;
import com.dbmanage.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 保存的查询仓库接口
 */
@Repository
public interface SavedQueryRepository extends JpaRepository<SavedQuery, Long> {
    
    /**
     * 根据用户查找保存的查询
     * @param user 用户
     * @return 保存的查询列表
     */
    List<SavedQuery> findByUser(User user);
    
    /**
     * 根据用户和名称查找保存的查询
     * @param user 用户
     * @param name 查询名称
     * @return 保存的查询列表
     */
    List<SavedQuery> findByUserAndName(User user, String name);
    
    /**
     * 根据用户和连接ID查找保存的查询
     * @param user 用户
     * @param connectionId 连接ID
     * @return 保存的查询列表
     */
    List<SavedQuery> findByUserAndConnectionId(User user, Long connectionId);
    
    /**
     * 根据ID和用户查找保存的查询
     * @param id 查询ID
     * @param user 用户
     * @return 保存的查询
     */
    SavedQuery findByIdAndUser(Long id, User user);
} 
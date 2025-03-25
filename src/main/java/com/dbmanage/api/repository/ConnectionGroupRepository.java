package com.dbmanage.api.repository;

import com.dbmanage.api.model.ConnectionGroup;
import com.dbmanage.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 数据库连接分组仓库接口
 */
@Repository
public interface ConnectionGroupRepository extends JpaRepository<ConnectionGroup, Long> {
    
    /**
     * 根据用户查找所有连接分组
     * @param user 用户
     * @return 连接分组列表
     */
    List<ConnectionGroup> findByUser(User user);
    
    /**
     * 根据用户和分组名称查找连接分组
     * @param user 用户
     * @param name 分组名称
     * @return 连接分组
     */
    Optional<ConnectionGroup> findByUserAndName(User user, String name);
    
    /**
     * 检查指定用户下是否存在指定名称的分组
     * @param user 用户
     * @param name 分组名称
     * @return 是否存在
     */
    boolean existsByUserAndName(User user, String name);
    
    /**
     * 根据ID和用户查找连接分组
     * @param id 分组ID
     * @param user 用户
     * @return 连接分组
     */
    Optional<ConnectionGroup> findByIdAndUser(Long id, User user);
} 
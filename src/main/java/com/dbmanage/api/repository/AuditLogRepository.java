package com.dbmanage.api.repository;

import com.dbmanage.api.model.AuditLog;
import com.dbmanage.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    
    Page<AuditLog> findByUserAndActionOrderByCreatedAtDesc(User user, String action, Pageable pageable);
} 
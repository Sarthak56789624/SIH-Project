package com.aichainid.audit.repository;

import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserId(Long userId);
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    List<AuditLog> findByEventType(AuditEventType eventType);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
}

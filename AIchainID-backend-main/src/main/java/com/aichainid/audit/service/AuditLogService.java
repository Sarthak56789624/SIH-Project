package com.aichainid.audit.service;

import com.aichainid.audit.dto.AuditLogResponse;
import com.aichainid.audit.entity.AuditEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    void log(Long userId, AuditEventType eventType, String entityType, Long entityId, String description);

    void log(AuditEventType eventType, String entityType, Long entityId, String description);

    List<AuditLogResponse> getAllLogs();

    Page<AuditLogResponse> getAllLogsPaged(Pageable pageable);

    AuditLogResponse getLogById(Long id);

    List<AuditLogResponse> getLogsByUserId(Long userId);

    List<AuditLogResponse> getLogsByEntity(String entityType, Long entityId);
}

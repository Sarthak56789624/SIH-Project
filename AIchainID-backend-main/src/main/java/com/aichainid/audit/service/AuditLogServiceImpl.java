package com.aichainid.audit.service;

import com.aichainid.audit.dto.AuditLogResponse;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.entity.AuditLog;
import com.aichainid.audit.mapper.AuditLogMapper;
import com.aichainid.audit.repository.AuditLogRepository;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.security.SecurityUtils;
import com.aichainid.user.entity.User;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, AuditEventType eventType, String entityType, Long entityId, String description) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
            String ip = SecurityUtils.getClientIpAddress();
            String ua = SecurityUtils.getClientUserAgent();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .eventType(eventType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ip)
                    .userAgent(ua)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("AUDIT: [{}] User: {}, Entity: {}/{}, Description: {}",
                    eventType, (user != null ? user.getEmail() : "SYSTEM"), entityType, entityId, description);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditEventType eventType, String entityType, Long entityId, String description) {
        Long currentUserId = null;
        try {
            currentUserId = SecurityUtils.getCurrentUserId();
        } catch (Exception ignored) {
            // Unauthenticated or system event
        }
        log(currentUserId, eventType, entityType, entityId, description);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllLogsPaged(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getLogById(Long id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return auditLogMapper.toResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId).stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(auditLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}

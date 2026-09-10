package com.aichainid.audit.dto;

import com.aichainid.audit.entity.AuditEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private AuditEventType eventType;
    private String entityType;
    private Long entityId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
}

package com.aichainid.audit.controller;

import com.aichainid.audit.dto.AuditLogResponse;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.response.ApiResponse;
import com.aichainid.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "System-wide automated audit trail and security event logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/api/audit-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'LAB_MANAGER')")
    @Operation(summary = "Get all audit logs (with pagination)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<AuditLogResponse> paged = auditLogService.getAllLogsPaged(PageRequest.of(page, size, sort));

        PagedResponse<AuditLogResponse> response = PagedResponse.<AuditLogResponse>builder()
                .content(paged.getContent())
                .pageNumber(paged.getNumber())
                .pageSize(paged.getSize())
                .totalElements(paged.getTotalElements())
                .totalPages(paged.getTotalPages())
                .first(paged.isFirst())
                .last(paged.isLast())
                .empty(paged.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }

    @GetMapping("/api/audit-logs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'LAB_MANAGER')")
    @Operation(summary = "Get audit log by ID")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(@PathVariable Long id) {
        AuditLogResponse log = auditLogService.getLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Audit log retrieved successfully", log));
    }

    @GetMapping("/api/users/{userId}/audit-logs")
    @Operation(summary = "Get all audit logs for a specific user")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByUserId(@PathVariable Long userId) {
        List<AuditLogResponse> logs = auditLogService.getLogsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User audit logs retrieved successfully", logs));
    }

    @GetMapping("/api/access-requests/{id}/audit-logs")
    @Operation(summary = "Get audit trail for a specific access request")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAccessRequestAuditLogs(@PathVariable Long id) {
        List<AuditLogResponse> logs = auditLogService.getLogsByEntity("ACCESS_REQUEST", id);
        return ResponseEntity.ok(ApiResponse.success("Access request audit logs retrieved successfully", logs));
    }
}

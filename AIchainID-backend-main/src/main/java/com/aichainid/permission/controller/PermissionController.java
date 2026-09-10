package com.aichainid.permission.controller;

import com.aichainid.common.response.ApiResponse;
import com.aichainid.permission.dto.PermissionCreateRequest;
import com.aichainid.permission.dto.PermissionResponse;
import com.aichainid.permission.dto.PermissionUpdateRequest;
import com.aichainid.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "System permission management APIs")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new permission (Admin only)")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse created = permissionService.createPermission(request);
        return new ResponseEntity<>(ApiResponse.created("Permission created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved successfully", permissions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success("Permission retrieved successfully", permission));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update permission (Admin only)")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionUpdateRequest request) {
        PermissionResponse updated = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete permission (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully"));
    }
}

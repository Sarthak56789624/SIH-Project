package com.aichainid.role.controller;

import com.aichainid.common.response.ApiResponse;
import com.aichainid.role.dto.RoleCreateRequest;
import com.aichainid.role.dto.RoleResponse;
import com.aichainid.role.dto.RoleUpdateRequest;
import com.aichainid.role.service.RoleService;
import com.aichainid.user.dto.UserResponse;
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
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role and Role-Permission mapping APIs")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/api/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new role (Admin only)")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse created = roleService.createRole(request);
        return new ResponseEntity<>(ApiResponse.created("Role created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/api/roles")
    @Operation(summary = "Get all roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @GetMapping("/api/roles/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success("Role retrieved successfully", role));
    }

    @PutMapping("/api/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role (Admin only)")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {
        RoleResponse updated = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    @DeleteMapping("/api/roles/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete role (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully"));
    }

    @PostMapping("/api/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign permission to role (Admin only)")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        RoleResponse updated = roleService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission assigned to role successfully", updated));
    }

    @DeleteMapping("/api/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove permission from role (Admin only)")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        RoleResponse updated = roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully", updated));
    }

    @PostMapping("/api/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        UserResponse updated = roleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role assigned to user successfully", updated));
    }

    @DeleteMapping("/api/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        UserResponse updated = roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed from user successfully", updated));
    }
}

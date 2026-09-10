package com.aichainid.role.mapper;

import com.aichainid.permission.dto.PermissionResponse;
import com.aichainid.permission.mapper.PermissionMapper;
import com.aichainid.role.dto.RoleCreateRequest;
import com.aichainid.role.dto.RoleResponse;
import com.aichainid.role.dto.RoleUpdateRequest;
import com.aichainid.role.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public Role toEntity(RoleCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Role.builder()
                .name(request.getName().trim().toUpperCase())
                .description(request.getDescription())
                .build();
    }

    public RoleResponse toResponse(Role role) {
        if (role == null) {
            return null;
        }

        Set<PermissionResponse> permissionResponses = role.getPermissions() != null
                ? role.getPermissions().stream().map(permissionMapper::toResponse).collect(Collectors.toSet())
                : Collections.emptySet();

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .organizationId(role.getOrganization() != null ? role.getOrganization().getId() : null)
                .organizationName(role.getOrganization() != null ? role.getOrganization().getName() : null)
                .permissions(permissionResponses)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    public void updateEntity(Role role, RoleUpdateRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            role.setName(request.getName().trim().toUpperCase());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
    }
}

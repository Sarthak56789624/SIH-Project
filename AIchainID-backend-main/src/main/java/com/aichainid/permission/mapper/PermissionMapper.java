package com.aichainid.permission.mapper;

import com.aichainid.permission.dto.PermissionCreateRequest;
import com.aichainid.permission.dto.PermissionResponse;
import com.aichainid.permission.dto.PermissionUpdateRequest;
import com.aichainid.permission.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Permission.builder()
                .name(request.getName().trim().toUpperCase())
                .description(request.getDescription())
                .resourceType(request.getResourceType())
                .action(request.getAction())
                .build();
    }

    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resourceType(permission.getResourceType())
                .action(permission.getAction())
                .createdAt(permission.getCreatedAt())
                .build();
    }

    public void updateEntity(Permission permission, PermissionUpdateRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            permission.setName(request.getName().trim().toUpperCase());
        }
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
        if (request.getResourceType() != null) {
            permission.setResourceType(request.getResourceType());
        }
        if (request.getAction() != null) {
            permission.setAction(request.getAction());
        }
    }
}

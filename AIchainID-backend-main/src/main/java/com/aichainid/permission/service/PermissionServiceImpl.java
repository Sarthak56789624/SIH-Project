package com.aichainid.permission.service;

import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.permission.dto.PermissionCreateRequest;
import com.aichainid.permission.dto.PermissionResponse;
import com.aichainid.permission.dto.PermissionUpdateRequest;
import com.aichainid.permission.entity.Permission;
import com.aichainid.permission.mapper.PermissionMapper;
import com.aichainid.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        String name = request.getName().trim().toUpperCase();
        if (permissionRepository.existsByName(name)) {
            throw new DuplicateResourceException("Permission", "name", name);
        }

        Permission permission = permissionMapper.toEntity(request);
        Permission saved = permissionRepository.save(permission);

        auditLogService.log(AuditEventType.PERMISSION_CREATED, "PERMISSION", saved.getId(),
                "Permission created: " + saved.getName());

        return permissionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));
        return permissionMapper.toResponse(permission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        if (request.getName() != null && !request.getName().trim().equalsIgnoreCase(permission.getName())) {
            String newName = request.getName().trim().toUpperCase();
            if (permissionRepository.existsByName(newName)) {
                throw new DuplicateResourceException("Permission", "name", newName);
            }
        }

        permissionMapper.updateEntity(permission, request);
        Permission updated = permissionRepository.save(permission);

        auditLogService.log(AuditEventType.PERMISSION_UPDATED, "PERMISSION", updated.getId(),
                "Permission updated: " + updated.getName());

        return permissionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", id));

        permissionRepository.delete(permission);
        auditLogService.log(AuditEventType.PERMISSION_DELETED, "PERMISSION", id,
                "Permission deleted: " + permission.getName());
    }
}

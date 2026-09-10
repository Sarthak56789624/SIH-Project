package com.aichainid.permission.service;

import com.aichainid.permission.dto.PermissionCreateRequest;
import com.aichainid.permission.dto.PermissionResponse;
import com.aichainid.permission.dto.PermissionUpdateRequest;

import java.util.List;

public interface PermissionService {

    PermissionResponse createPermission(PermissionCreateRequest request);

    List<PermissionResponse> getAllPermissions();

    PermissionResponse getPermissionById(Long id);

    PermissionResponse updatePermission(Long id, PermissionUpdateRequest request);

    void deletePermission(Long id);
}

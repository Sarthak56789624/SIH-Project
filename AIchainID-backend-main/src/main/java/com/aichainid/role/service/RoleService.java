package com.aichainid.role.service;

import com.aichainid.role.dto.RoleCreateRequest;
import com.aichainid.role.dto.RoleResponse;
import com.aichainid.role.dto.RoleUpdateRequest;
import com.aichainid.user.dto.UserResponse;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(RoleCreateRequest request);

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Long id);

    RoleResponse updateRole(Long id, RoleUpdateRequest request);

    void deleteRole(Long id);

    RoleResponse assignPermissionToRole(Long roleId, Long permissionId);

    RoleResponse removePermissionFromRole(Long roleId, Long permissionId);

    UserResponse assignRoleToUser(Long userId, Long roleId);

    UserResponse removeRoleFromUser(Long userId, Long roleId);
}

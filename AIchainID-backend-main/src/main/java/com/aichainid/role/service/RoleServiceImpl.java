package com.aichainid.role.service;

import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.permission.entity.Permission;
import com.aichainid.permission.repository.PermissionRepository;
import com.aichainid.role.dto.RoleCreateRequest;
import com.aichainid.role.dto.RoleResponse;
import com.aichainid.role.dto.RoleUpdateRequest;
import com.aichainid.role.entity.Role;
import com.aichainid.role.mapper.RoleMapper;
import com.aichainid.role.repository.RoleRepository;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.entity.User;
import com.aichainid.user.mapper.UserMapper;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final BlockchainService blockchainService;

    @Override
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        String roleName = request.getName().trim().toUpperCase();

        if (request.getOrganizationId() != null) {
            if (roleRepository.existsByNameAndOrganizationId(roleName, request.getOrganizationId())) {
                throw new DuplicateResourceException("Role", "name", roleName);
            }
        } else if (roleRepository.existsByName(roleName)) {
            throw new DuplicateResourceException("Role", "name", roleName);
        }

        Role role = roleMapper.toEntity(request);

        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            role.setOrganization(org);
        }

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        Role saved = roleRepository.save(role);

        auditLogService.log(AuditEventType.ROLE_CREATED, "ROLE", saved.getId(), "Role created: " + saved.getName());

        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        roleMapper.updateEntity(role, request);

        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            role.setOrganization(org);
        }

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(permissions);
        }

        Role updated = roleRepository.save(role);

        auditLogService.log(AuditEventType.ROLE_UPDATED, "ROLE", updated.getId(), "Role updated: " + updated.getName());

        return roleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        roleRepository.delete(role);
        auditLogService.log(AuditEventType.ROLE_DELETED, "ROLE", id, "Role deleted: " + role.getName());
    }

    @Override
    @Transactional
    public RoleResponse assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.getPermissions().add(permission);
        Role saved = roleRepository.save(role);

        String recordHash = CryptoUtils.sha256Hex("PERMISSION_ASSIGNED:" + roleId + ":" + permissionId + ":" + System.currentTimeMillis());
        blockchainService.recordPermissionChange(roleId, recordHash);

        auditLogService.log(AuditEventType.PERMISSION_ASSIGNED, "ROLE", roleId,
                "Assigned permission " + permission.getName() + " to role " + role.getName());

        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RoleResponse removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.getPermissions().remove(permission);
        Role saved = roleRepository.save(role);

        String recordHash = CryptoUtils.sha256Hex("PERMISSION_REMOVED:" + roleId + ":" + permissionId + ":" + System.currentTimeMillis());
        blockchainService.recordPermissionChange(roleId, recordHash);

        auditLogService.log(AuditEventType.PERMISSION_REMOVED, "ROLE", roleId,
                "Removed permission " + permission.getName() + " from role " + role.getName());

        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().add(role);
        User saved = userRepository.save(user);

        auditLogService.log(AuditEventType.ROLE_ASSIGNED, "USER", userId,
                "Assigned role " + role.getName() + " to user " + user.getEmail());

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        user.getRoles().remove(role);
        User saved = userRepository.save(user);

        auditLogService.log(AuditEventType.ROLE_REMOVED, "USER", userId,
                "Removed role " + role.getName() + " from user " + user.getEmail());

        return userMapper.toResponse(saved);
    }
}

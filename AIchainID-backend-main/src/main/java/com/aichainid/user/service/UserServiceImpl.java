package com.aichainid.user.service;

import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.access.mapper.AccessRequestMapper;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.asset.dto.AssetAssignmentResponse;
import com.aichainid.asset.mapper.AssetMapper;
import com.aichainid.asset.repository.AssetAssignmentRepository;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.credential.mapper.CredentialMapper;
import com.aichainid.credential.repository.CredentialRepository;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.role.entity.Role;
import com.aichainid.role.repository.RoleRepository;
import com.aichainid.user.dto.UserCreateRequest;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.dto.UserUpdateRequest;
import com.aichainid.user.entity.User;
import com.aichainid.user.entity.UserStatus;
import com.aichainid.user.mapper.UserMapper;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CredentialMapper credentialMapper;
    private final AssetMapper assetMapper;
    private final AccessRequestMapper accessRequestMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encodedPassword);
        user.setOrganization(org);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        } else {
            roleRepository.findByName("STUDENT").ifPresent(role -> user.getRoles().add(role));
        }

        User saved = userRepository.save(user);

        auditLogService.log(saved.getId(), AuditEventType.USER_CREATED, "USER", saved.getId(),
                "User created: " + saved.getEmail());

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getEmail() != null && !request.getEmail().trim().equalsIgnoreCase(user.getEmail())) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User", "email", newEmail);
            }
        }

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        userMapper.updateEntity(user, request, encodedPassword);

        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            user.setOrganization(org);
        }

        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        }

        User updated = userRepository.save(user);

        auditLogService.log(updated.getId(), AuditEventType.USER_UPDATED, "USER", updated.getId(),
                "User updated: " + updated.getEmail());

        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        auditLogService.log(id, AuditEventType.USER_DELETED, "USER", id,
                "User deactivated: " + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getUserRoles(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialResponse> getUserCredentials(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return credentialRepository.findByUserId(id).stream()
                .map(credentialMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAssignmentResponse> getUserAssets(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return assetAssignmentRepository.findByUserId(id).stream()
                .map(assetMapper::toAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessRequestResponse> getUserAccessRequests(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return accessRequestRepository.findByRequesterId(id).stream()
                .map(accessRequestMapper::toResponse)
                .collect(Collectors.toList());
    }
}

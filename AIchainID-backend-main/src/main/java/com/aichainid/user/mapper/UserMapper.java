package com.aichainid.user.mapper;

import com.aichainid.identity.repository.IdentityRepository;
import com.aichainid.role.entity.Role;
import com.aichainid.user.dto.UserCreateRequest;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.dto.UserUpdateRequest;
import com.aichainid.user.entity.User;
import com.aichainid.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final IdentityRepository identityRepository;

    public User toEntity(UserCreateRequest request, String encodedPassword) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(encodedPassword)
                .phone(request.getPhone())
                .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
                .build();
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Collections.emptySet();

        String did = identityRepository.findByUserId(user.getId())
                .map(i -> i.getDid())
                .orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .did(did)
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public void updateEntity(User user, UserUpdateRequest request, String encodedPassword) {
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (encodedPassword != null && !encodedPassword.isBlank()) {
            user.setPasswordHash(encodedPassword);
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
    }
}

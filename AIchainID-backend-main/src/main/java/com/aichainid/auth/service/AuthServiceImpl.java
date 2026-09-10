package com.aichainid.auth.service;

import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.auth.dto.AuthResponse;
import com.aichainid.auth.dto.LoginRequest;
import com.aichainid.auth.dto.RegisterRequest;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.role.entity.Role;
import com.aichainid.role.repository.RoleRepository;
import com.aichainid.security.CustomUserDetails;
import com.aichainid.security.JwtService;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.entity.User;
import com.aichainid.user.entity.UserStatus;
import com.aichainid.user.mapper.UserMapper;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .organization(organization)
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>())
                .build();

        // Default role assignment if STUDENT role exists
        roleRepository.findByName("STUDENT").ifPresent(role -> user.getRoles().add(role));

        User saved = userRepository.save(user);

        auditLogService.log(saved.getId(), AuditEventType.USER_REGISTERED, "USER", saved.getId(),
                "User registered: " + saved.getEmail());

        return userMapper.toResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails, userDetails.getId());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList());

            auditLogService.log(userDetails.getId(), AuditEventType.LOGIN_SUCCESS, "USER", userDetails.getId(),
                    "User logged in successfully: " + email);

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTimeInSeconds())
                    .userId(userDetails.getId())
                    .email(userDetails.getEmail())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .organizationId(userDetails.getOrganizationId())
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException e) {
            auditLogService.log(AuditEventType.LOGIN_FAILED, "USER", null,
                    "Failed login attempt for email: " + email);
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}

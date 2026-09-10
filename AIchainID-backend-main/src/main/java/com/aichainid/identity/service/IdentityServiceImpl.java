package com.aichainid.identity.service;

import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.identity.dto.IdentityCreateRequest;
import com.aichainid.identity.dto.IdentityResponse;
import com.aichainid.identity.dto.IdentityStatusUpdateRequest;
import com.aichainid.identity.dto.IdentityVerificationResponse;
import com.aichainid.identity.entity.Identity;
import com.aichainid.identity.entity.IdentityStatus;
import com.aichainid.identity.mapper.IdentityMapper;
import com.aichainid.identity.repository.IdentityRepository;
import com.aichainid.user.entity.User;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityServiceImpl implements IdentityService {

    private final IdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final IdentityMapper identityMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public IdentityResponse createIdentity(IdentityCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        if (identityRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("Identity already exists for user ID: " + user.getId());
        }

        String did = CryptoUtils.generateDid();
        String publicKey = CryptoUtils.generatePublicKey();
        String method = request.getDidMethod() != null ? request.getDidMethod() : "did:chainid";

        Identity identity = Identity.builder()
                .user(user)
                .did(did)
                .publicKey(publicKey)
                .didMethod(method)
                .status(IdentityStatus.ACTIVE)
                .build();

        Identity saved = identityRepository.save(identity);

        auditLogService.log(user.getId(), AuditEventType.DID_CREATED, "IDENTITY", saved.getId(),
                "Generated DID: " + did + " for user " + user.getEmail());

        return identityMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IdentityResponse getIdentityByUserId(Long userId) {
        Identity identity = identityRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Identity", "userId", userId));
        return identityMapper.toResponse(identity);
    }

    @Override
    @Transactional(readOnly = true)
    public IdentityResponse getIdentityByDid(String did) {
        Identity identity = identityRepository.findByDid(did)
                .orElseThrow(() -> new ResourceNotFoundException("Identity", "did", did));
        return identityMapper.toResponse(identity);
    }

    @Override
    @Transactional
    public IdentityVerificationResponse verifyIdentity(Long id) {
        Identity identity = identityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Identity", "id", id));

        boolean isValid = identity.getStatus() == IdentityStatus.ACTIVE
                && identity.getPublicKey() != null
                && !identity.getPublicKey().isBlank()
                && identity.getUser() != null;

        String message = isValid ? "Decentralized Identity is valid and active" : "Identity is invalid or inactive (status: " + identity.getStatus() + ")";

        auditLogService.log(identity.getUser().getId(), AuditEventType.DID_VERIFIED, "IDENTITY", identity.getId(),
                "Verified DID: " + identity.getDid() + " - " + message);

        return IdentityVerificationResponse.builder()
                .valid(isValid)
                .did(identity.getDid())
                .userId(identity.getUser().getId())
                .userEmail(identity.getUser().getEmail())
                .status(identity.getStatus())
                .message(message)
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public IdentityResponse updateIdentityStatus(Long id, IdentityStatusUpdateRequest request) {
        Identity identity = identityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Identity", "id", id));

        identity.setStatus(request.getStatus());
        Identity updated = identityRepository.save(identity);

        auditLogService.log(identity.getUser().getId(), AuditEventType.DID_STATUS_UPDATED, "IDENTITY", updated.getId(),
                "Updated DID status to " + request.getStatus() + " for DID: " + identity.getDid());

        return identityMapper.toResponse(updated);
    }
}

package com.aichainid.credential.service;

import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.BadRequestException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.credential.dto.CredentialCreateRequest;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.credential.dto.CredentialUpdateRequest;
import com.aichainid.credential.dto.CredentialVerificationResponse;
import com.aichainid.credential.entity.Credential;
import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.mapper.CredentialMapper;
import com.aichainid.credential.repository.CredentialRepository;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.user.entity.User;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialServiceImpl implements CredentialService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CredentialMapper credentialMapper;
    private final BlockchainService blockchainService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CredentialResponse issueCredential(CredentialCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Organization issuer = organizationRepository.findById(request.getIssuerOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getIssuerOrganizationId()));

        String credentialId = "cred-" + UUID.randomUUID().toString();
        String payloadToHash = credentialId + ":" + user.getId() + ":" + issuer.getId() + ":" + request.getCredentialType() + ":" + request.getTitle();
        String credentialHash = CryptoUtils.sha256Hex(payloadToHash);

        Credential credential = credentialMapper.toEntity(request, credentialId, credentialHash);
        credential.setUser(user);
        credential.setIssuerOrganization(issuer);

        Credential saved = credentialRepository.save(credential);

        // Record SHA-256 integrity hash on blockchain ledger
        blockchainService.recordCredentialHash(saved.getId(), credentialHash);

        // Record audit trail
        auditLogService.log(user.getId(), AuditEventType.CREDENTIAL_ISSUED, "CREDENTIAL", saved.getId(),
                "Issued " + saved.getCredentialType() + " credential (" + saved.getTitle() + ") by " + issuer.getName());

        return credentialMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialResponse> getAllCredentials() {
        return credentialRepository.findAll().stream()
                .map(credentialMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialResponse getCredentialById(Long id) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));
        return credentialMapper.toResponse(credential);
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialResponse getCredentialByCredentialId(String credentialId) {
        Credential credential = credentialRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "credentialId", credentialId));
        return credentialMapper.toResponse(credential);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CredentialResponse> getCredentialsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return credentialRepository.findByUserId(userId).stream()
                .map(credentialMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CredentialVerificationResponse verifyCredential(Long id) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));

        boolean isExpired = credential.getExpiryDate() != null && credential.getExpiryDate().isBefore(LocalDateTime.now());
        boolean isRevoked = credential.getStatus() == CredentialStatus.REVOKED;

        // Verify cryptographic hash integrity
        String expectedPayload = credential.getCredentialId() + ":" + credential.getUser().getId() + ":" +
                credential.getIssuerOrganization().getId() + ":" + credential.getCredentialType() + ":" + credential.getTitle();
        String expectedHash = CryptoUtils.sha256Hex(expectedPayload);
        boolean hashIntact = expectedHash.equalsIgnoreCase(credential.getCredentialHash());

        if (isExpired && credential.getStatus() == CredentialStatus.ACTIVE) {
            credential.setStatus(CredentialStatus.EXPIRED);
            credentialRepository.save(credential);
        }

        boolean isValid = (!isExpired) && (!isRevoked) && hashIntact && (credential.getStatus() == CredentialStatus.ACTIVE);

        String message;
        if (!hashIntact) {
            message = "Credential failed cryptographic hash integrity check (tampered)";
        } else if (isRevoked) {
            message = "Credential has been revoked by issuer";
        } else if (isExpired) {
            message = "Credential has expired on " + credential.getExpiryDate();
        } else {
            message = "Credential is verified, authentic, and actively valid";
        }

        auditLogService.log(credential.getUser().getId(), AuditEventType.CREDENTIAL_VERIFIED, "CREDENTIAL", credential.getId(),
                "Verified credential " + credential.getCredentialId() + ": " + message);

        return CredentialVerificationResponse.builder()
                .valid(isValid)
                .credentialId(credential.getCredentialId())
                .title(credential.getTitle())
                .status(credential.getStatus())
                .expired(isExpired)
                .revoked(isRevoked)
                .hashIntact(hashIntact)
                .message(message)
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public CredentialResponse revokeCredential(Long id, String reason) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));

        if (credential.getStatus() == CredentialStatus.REVOKED) {
            throw new BadRequestException("Credential is already revoked");
        }

        credential.setStatus(CredentialStatus.REVOKED);
        Credential saved = credentialRepository.save(credential);

        String revocationHash = CryptoUtils.sha256Hex("CREDENTIAL_REVOKED:" + id + ":" + reason + ":" + System.currentTimeMillis());
        blockchainService.recordEvent("CREDENTIAL_REVOKED", "CREDENTIAL", id, revocationHash);

        auditLogService.log(credential.getUser().getId(), AuditEventType.CREDENTIAL_REVOKED, "CREDENTIAL", id,
                "Revoked credential " + credential.getCredentialId() + ". Reason: " + (reason != null ? reason : "No reason provided"));

        return credentialMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CredentialResponse updateCredential(Long id, CredentialUpdateRequest request) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential", "id", id));

        credentialMapper.updateEntity(credential, request);
        Credential updated = credentialRepository.save(credential);

        auditLogService.log(credential.getUser().getId(), AuditEventType.CREDENTIAL_UPDATED, "CREDENTIAL", id,
                "Updated credential metadata for " + credential.getCredentialId());

        return credentialMapper.toResponse(updated);
    }
}

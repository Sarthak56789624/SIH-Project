package com.aichainid.credential.mapper;

import com.aichainid.credential.dto.CredentialCreateRequest;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.credential.dto.CredentialUpdateRequest;
import com.aichainid.credential.entity.Credential;
import com.aichainid.credential.entity.CredentialStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CredentialMapper {

    public Credential toEntity(CredentialCreateRequest request, String credentialId, String credentialHash) {
        if (request == null) {
            return null;
        }
        return Credential.builder()
                .credentialId(credentialId)
                .credentialType(request.getCredentialType())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .credentialHash(credentialHash)
                .issuedAt(LocalDateTime.now())
                .expiryDate(request.getExpiryDate())
                .status(CredentialStatus.ACTIVE)
                .metadata(request.getMetadata())
                .build();
    }

    public CredentialResponse toResponse(Credential credential) {
        if (credential == null) {
            return null;
        }
        return CredentialResponse.builder()
                .id(credential.getId())
                .credentialId(credential.getCredentialId())
                .userId(credential.getUser() != null ? credential.getUser().getId() : null)
                .userEmail(credential.getUser() != null ? credential.getUser().getEmail() : null)
                .userName(credential.getUser() != null ? credential.getUser().getFirstName() + " " + credential.getUser().getLastName() : null)
                .issuerOrganizationId(credential.getIssuerOrganization() != null ? credential.getIssuerOrganization().getId() : null)
                .issuerOrganizationName(credential.getIssuerOrganization() != null ? credential.getIssuerOrganization().getName() : null)
                .credentialType(credential.getCredentialType())
                .title(credential.getTitle())
                .description(credential.getDescription())
                .credentialHash(credential.getCredentialHash())
                .issuedAt(credential.getIssuedAt())
                .expiryDate(credential.getExpiryDate())
                .status(credential.getStatus())
                .metadata(credential.getMetadata())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }

    public void updateEntity(Credential credential, CredentialUpdateRequest request) {
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            credential.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            credential.setDescription(request.getDescription());
        }
        if (request.getCredentialType() != null) {
            credential.setCredentialType(request.getCredentialType());
        }
        if (request.getExpiryDate() != null) {
            credential.setExpiryDate(request.getExpiryDate());
        }
        if (request.getStatus() != null) {
            credential.setStatus(request.getStatus());
        }
        if (request.getMetadata() != null) {
            credential.setMetadata(request.getMetadata());
        }
    }
}

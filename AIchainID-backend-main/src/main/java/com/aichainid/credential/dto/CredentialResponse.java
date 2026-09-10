package com.aichainid.credential.dto;

import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.entity.CredentialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialResponse {

    private Long id;
    private String credentialId;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long issuerOrganizationId;
    private String issuerOrganizationName;
    private CredentialType credentialType;
    private String title;
    private String description;
    private String credentialHash;
    private LocalDateTime issuedAt;
    private LocalDateTime expiryDate;
    private CredentialStatus status;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

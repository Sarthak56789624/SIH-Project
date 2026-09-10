package com.aichainid.credential.dto;

import com.aichainid.credential.entity.CredentialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialVerificationResponse {

    private boolean valid;
    private String credentialId;
    private String title;
    private CredentialStatus status;
    private boolean expired;
    private boolean revoked;
    private boolean hashIntact;
    private String message;
    private LocalDateTime verifiedAt;
}

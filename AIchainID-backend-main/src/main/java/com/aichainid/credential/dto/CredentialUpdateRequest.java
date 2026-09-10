package com.aichainid.credential.dto;

import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.entity.CredentialType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialUpdateRequest {

    @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
    private String title;

    private String description;

    private CredentialType credentialType;

    private LocalDateTime expiryDate;

    private CredentialStatus status;

    private String metadata;
}

package com.aichainid.credential.dto;

import com.aichainid.credential.entity.CredentialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CredentialCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Issuer Organization ID is required")
    private Long issuerOrganizationId;

    @NotNull(message = "Credential type is required")
    private CredentialType credentialType;

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
    private String title;

    private String description;

    private LocalDateTime expiryDate;

    private String metadata;
}

package com.aichainid.identity.dto;

import com.aichainid.identity.entity.IdentityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationResponse {

    private boolean valid;
    private String did;
    private Long userId;
    private String userEmail;
    private IdentityStatus status;
    private String message;
    private LocalDateTime verifiedAt;
}

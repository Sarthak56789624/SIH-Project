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
public class IdentityResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private String did;
    private String publicKey;
    private String didMethod;
    private IdentityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

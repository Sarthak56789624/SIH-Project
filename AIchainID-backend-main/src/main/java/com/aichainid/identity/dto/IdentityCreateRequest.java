package com.aichainid.identity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String didMethod;
}

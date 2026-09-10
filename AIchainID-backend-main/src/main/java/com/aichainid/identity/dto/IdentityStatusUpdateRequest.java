package com.aichainid.identity.dto;

import com.aichainid.identity.entity.IdentityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private IdentityStatus status;
}

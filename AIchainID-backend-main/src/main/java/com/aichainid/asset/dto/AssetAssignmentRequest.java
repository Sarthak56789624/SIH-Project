package com.aichainid.asset.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAssignmentRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String notes;
}

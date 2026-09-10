package com.aichainid.asset.dto;

import com.aichainid.asset.entity.AssetStatus;
import com.aichainid.asset.entity.AssetType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetUpdateRequest {

    @Size(min = 2, max = 50, message = "Asset code must be between 2 and 50 characters")
    private String assetCode;

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    private String description;

    private AssetType assetType;

    private Long organizationId;

    private AssetStatus status;

    private String serialNumber;
}

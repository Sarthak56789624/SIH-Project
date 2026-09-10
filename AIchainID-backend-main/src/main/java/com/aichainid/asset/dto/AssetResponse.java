package com.aichainid.asset.dto;

import com.aichainid.asset.entity.AssetStatus;
import com.aichainid.asset.entity.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {

    private Long id;
    private String assetCode;
    private String name;
    private String description;
    private AssetType assetType;
    private Long organizationId;
    private String organizationName;
    private AssetStatus status;
    private String serialNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

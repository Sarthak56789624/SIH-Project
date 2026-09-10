package com.aichainid.asset.mapper;

import com.aichainid.asset.dto.AssetAssignmentResponse;
import com.aichainid.asset.dto.AssetCreateRequest;
import com.aichainid.asset.dto.AssetResponse;
import com.aichainid.asset.dto.AssetUpdateRequest;
import com.aichainid.asset.entity.Asset;
import com.aichainid.asset.entity.AssetAssignment;
import com.aichainid.asset.entity.AssetStatus;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

    public Asset toEntity(AssetCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Asset.builder()
                .assetCode(request.getAssetCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription())
                .assetType(request.getAssetType())
                .status(request.getStatus() != null ? request.getStatus() : AssetStatus.AVAILABLE)
                .serialNumber(request.getSerialNumber())
                .build();
    }

    public AssetResponse toResponse(Asset asset) {
        if (asset == null) {
            return null;
        }
        return AssetResponse.builder()
                .id(asset.getId())
                .assetCode(asset.getAssetCode())
                .name(asset.getName())
                .description(asset.getDescription())
                .assetType(asset.getAssetType())
                .organizationId(asset.getOrganization() != null ? asset.getOrganization().getId() : null)
                .organizationName(asset.getOrganization() != null ? asset.getOrganization().getName() : null)
                .status(asset.getStatus())
                .serialNumber(asset.getSerialNumber())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }

    public void updateEntity(Asset asset, AssetUpdateRequest request) {
        if (request.getAssetCode() != null && !request.getAssetCode().isBlank()) {
            asset.setAssetCode(request.getAssetCode().trim().toUpperCase());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            asset.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            asset.setDescription(request.getDescription());
        }
        if (request.getAssetType() != null) {
            asset.setAssetType(request.getAssetType());
        }
        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        if (request.getSerialNumber() != null) {
            asset.setSerialNumber(request.getSerialNumber());
        }
    }

    public AssetAssignmentResponse toAssignmentResponse(AssetAssignment assignment) {
        if (assignment == null) {
            return null;
        }
        return AssetAssignmentResponse.builder()
                .id(assignment.getId())
                .assetId(assignment.getAsset().getId())
                .assetCode(assignment.getAsset().getAssetCode())
                .assetName(assignment.getAsset().getName())
                .userId(assignment.getUser().getId())
                .userEmail(assignment.getUser().getEmail())
                .userName(assignment.getUser().getFirstName() + " " + assignment.getUser().getLastName())
                .assignedById(assignment.getAssignedBy().getId())
                .assignedByName(assignment.getAssignedBy().getFirstName() + " " + assignment.getAssignedBy().getLastName())
                .assignedAt(assignment.getAssignedAt())
                .returnedAt(assignment.getReturnedAt())
                .status(assignment.getStatus())
                .notes(assignment.getNotes())
                .build();
    }
}

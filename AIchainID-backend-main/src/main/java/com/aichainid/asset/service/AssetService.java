package com.aichainid.asset.service;

import com.aichainid.asset.dto.*;

import java.util.List;

public interface AssetService {

    AssetResponse createAsset(AssetCreateRequest request);

    List<AssetResponse> getAllAssets();

    AssetResponse getAssetById(Long id);

    AssetResponse updateAsset(Long id, AssetUpdateRequest request);

    void deleteAsset(Long id);

    AssetAssignmentResponse assignAsset(Long assetId, AssetAssignmentRequest request);

    AssetAssignmentResponse returnAsset(Long assetId, String notes);

    List<AssetAssignmentResponse> getAssetHistory(Long assetId);

    List<AssetAssignmentResponse> getAssetsByUserId(Long userId);
}

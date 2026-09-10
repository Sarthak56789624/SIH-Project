package com.aichainid.asset.controller;

import com.aichainid.asset.dto.*;
import com.aichainid.asset.service.AssetService;
import com.aichainid.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Assets", description = "Hardware, badge, and laboratory asset lifecycle management APIs")
public class AssetController {

    private final AssetService assetService;

    @PostMapping("/api/assets")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
    @Operation(summary = "Register a new asset (Admin/Lab Manager)")
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(@Valid @RequestBody AssetCreateRequest request) {
        AssetResponse created = assetService.createAsset(request);
        return new ResponseEntity<>(ApiResponse.created("Asset registered successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/api/assets")
    @Operation(summary = "Get all assets")
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getAllAssets() {
        List<AssetResponse> assets = assetService.getAllAssets();
        return ResponseEntity.ok(ApiResponse.success("Assets retrieved successfully", assets));
    }

    @GetMapping("/api/assets/{id}")
    @Operation(summary = "Get asset by ID")
    public ResponseEntity<ApiResponse<AssetResponse>> getAssetById(@PathVariable Long id) {
        AssetResponse asset = assetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.success("Asset retrieved successfully", asset));
    }

    @PutMapping("/api/assets/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
    @Operation(summary = "Update asset details (Admin/Lab Manager)")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest request) {
        AssetResponse updated = assetService.updateAsset(id, request);
        return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", updated));
    }

    @DeleteMapping("/api/assets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retire asset (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset retired successfully"));
    }

    @PostMapping("/api/assets/{assetId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'FACULTY')")
    @Operation(summary = "Assign asset to a user (Admin/Lab Manager/Faculty)")
    public ResponseEntity<ApiResponse<AssetAssignmentResponse>> assignAsset(
            @PathVariable Long assetId,
            @Valid @RequestBody AssetAssignmentRequest request) {
        AssetAssignmentResponse response = assetService.assignAsset(assetId, request);
        return ResponseEntity.ok(ApiResponse.success("Asset assigned successfully", response));
    }

    @PostMapping("/api/assets/{assetId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'FACULTY')")
    @Operation(summary = "Process return of an assigned asset")
    public ResponseEntity<ApiResponse<AssetAssignmentResponse>> returnAsset(
            @PathVariable Long assetId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        AssetAssignmentResponse response = assetService.returnAsset(assetId, notes);
        return ResponseEntity.ok(ApiResponse.success("Asset returned successfully", response));
    }

    @GetMapping("/api/assets/{assetId}/history")
    @Operation(summary = "Get assignment history for an asset")
    public ResponseEntity<ApiResponse<List<AssetAssignmentResponse>>> getAssetHistory(@PathVariable Long assetId) {
        List<AssetAssignmentResponse> history = assetService.getAssetHistory(assetId);
        return ResponseEntity.ok(ApiResponse.success("Asset assignment history retrieved successfully", history));
    }

    @GetMapping("/api/users/{userId}/assets")
    @Operation(summary = "Get all assets assigned to a user")
    public ResponseEntity<ApiResponse<List<AssetAssignmentResponse>>> getAssetsByUserId(@PathVariable Long userId) {
        List<AssetAssignmentResponse> assets = assetService.getAssetsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User assets retrieved successfully", assets));
    }
}

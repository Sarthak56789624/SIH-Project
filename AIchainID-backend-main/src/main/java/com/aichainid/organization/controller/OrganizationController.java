package com.aichainid.organization.controller;

import com.aichainid.asset.dto.AssetResponse;
import com.aichainid.common.response.ApiResponse;
import com.aichainid.organization.dto.OrganizationCreateRequest;
import com.aichainid.organization.dto.OrganizationResponse;
import com.aichainid.organization.dto.OrganizationUpdateRequest;
import com.aichainid.organization.service.OrganizationService;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Organization management APIs")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new organization (Admin only)")
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request) {
        OrganizationResponse created = organizationService.createOrganization(request);
        return new ResponseEntity<>(ApiResponse.created("Organization created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all organizations")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.success("Organizations retrieved successfully", organizations));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable Long id) {
        OrganizationResponse organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(ApiResponse.success("Organization retrieved successfully", organization));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an organization (Admin only)")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationUpdateRequest request) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate an organization (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(ApiResponse.success("Organization deactivated successfully"));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "Get all users belonging to an organization")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getOrganizationUsers(@PathVariable Long id) {
        List<UserResponse> users = organizationService.getOrganizationUsers(id);
        return ResponseEntity.ok(ApiResponse.success("Organization users retrieved successfully", users));
    }

    @GetMapping("/{id}/resources")
    @Operation(summary = "Get all resources belonging to an organization")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getOrganizationResources(@PathVariable Long id) {
        List<ResourceResponse> resources = organizationService.getOrganizationResources(id);
        return ResponseEntity.ok(ApiResponse.success("Organization resources retrieved successfully", resources));
    }

    @GetMapping("/{id}/assets")
    @Operation(summary = "Get all assets belonging to an organization")
    public ResponseEntity<ApiResponse<List<AssetResponse>>> getOrganizationAssets(@PathVariable Long id) {
        List<AssetResponse> assets = organizationService.getOrganizationAssets(id);
        return ResponseEntity.ok(ApiResponse.success("Organization assets retrieved successfully", assets));
    }
}

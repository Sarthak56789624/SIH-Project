package com.aichainid.resource.controller;

import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.common.response.ApiResponse;
import com.aichainid.resource.dto.ResourceCreateRequest;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.resource.dto.ResourceUpdateRequest;
import com.aichainid.resource.service.ResourceService;
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
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Tag(name = "Resources", description = "Target resource management APIs (Labs, Servers, Databases)")
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'LAB_MANAGER')")
    @Operation(summary = "Create a new resource (Admin/Lab Manager)")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @Valid @RequestBody ResourceCreateRequest request) {
        ResourceResponse created = resourceService.createResource(request);
        return new ResponseEntity<>(ApiResponse.created("Resource created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all resources")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getAllResources() {
        List<ResourceResponse> resources = resourceService.getAllResources();
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", resources));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(@PathVariable Long id) {
        ResourceResponse resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(ApiResponse.success("Resource retrieved successfully", resource));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
    @Operation(summary = "Update resource (Admin/Lab Manager)")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceUpdateRequest request) {
        ResourceResponse updated = resourceService.updateResource(id, request);
        return ResponseEntity.ok(ApiResponse.success("Resource updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate resource (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.success("Resource marked offline successfully"));
    }

    @GetMapping("/{id}/access-requests")
    @Operation(summary = "Get all access requests for a resource")
    public ResponseEntity<ApiResponse<List<AccessRequestResponse>>> getResourceAccessRequests(@PathVariable Long id) {
        List<AccessRequestResponse> requests = resourceService.getResourceAccessRequests(id);
        return ResponseEntity.ok(ApiResponse.success("Resource access requests retrieved successfully", requests));
    }
}

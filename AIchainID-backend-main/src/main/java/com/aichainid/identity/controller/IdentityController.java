package com.aichainid.identity.controller;

import com.aichainid.common.response.ApiResponse;
import com.aichainid.identity.dto.IdentityCreateRequest;
import com.aichainid.identity.dto.IdentityResponse;
import com.aichainid.identity.dto.IdentityStatusUpdateRequest;
import com.aichainid.identity.dto.IdentityVerificationResponse;
import com.aichainid.identity.service.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identities")
@RequiredArgsConstructor
@Tag(name = "Identities / DIDs", description = "Decentralized Identifier (DID) and public key management APIs")
public class IdentityController {

    private final IdentityService identityService;

    @PostMapping
    @Operation(summary = "Generate and create a DID for a user")
    public ResponseEntity<ApiResponse<IdentityResponse>> createIdentity(@Valid @RequestBody IdentityCreateRequest request) {
        IdentityResponse created = identityService.createIdentity(request);
        return new ResponseEntity<>(ApiResponse.created("Decentralized Identity created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get identity by user ID")
    public ResponseEntity<ApiResponse<IdentityResponse>> getIdentityByUserId(@PathVariable Long userId) {
        IdentityResponse identity = identityService.getIdentityByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Identity retrieved successfully", identity));
    }

    @GetMapping("/did/{did}")
    @Operation(summary = "Lookup identity by DID string")
    public ResponseEntity<ApiResponse<IdentityResponse>> getIdentityByDid(@PathVariable String did) {
        IdentityResponse identity = identityService.getIdentityByDid(did);
        return ResponseEntity.ok(ApiResponse.success("Identity retrieved successfully", identity));
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify cryptographic DID validity and active status")
    public ResponseEntity<ApiResponse<IdentityVerificationResponse>> verifyIdentity(@PathVariable Long id) {
        IdentityVerificationResponse verification = identityService.verifyIdentity(id);
        return ResponseEntity.ok(ApiResponse.success("Identity verification completed", verification));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update identity status (ACTIVE, INACTIVE, SUSPENDED, REVOKED)")
    public ResponseEntity<ApiResponse<IdentityResponse>> updateIdentityStatus(
            @PathVariable Long id,
            @Valid @RequestBody IdentityStatusUpdateRequest request) {
        IdentityResponse updated = identityService.updateIdentityStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Identity status updated successfully", updated));
    }
}

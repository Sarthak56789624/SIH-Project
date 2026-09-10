package com.aichainid.credential.controller;

import com.aichainid.common.response.ApiResponse;
import com.aichainid.credential.dto.CredentialCreateRequest;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.credential.dto.CredentialUpdateRequest;
import com.aichainid.credential.dto.CredentialVerificationResponse;
import com.aichainid.credential.service.CredentialService;
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
@Tag(name = "Credentials", description = "Verifiable Credential issuance, verification and revocation APIs")
public class CredentialController {

    private final CredentialService credentialService;

    @PostMapping("/api/credentials")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'LAB_MANAGER')")
    @Operation(summary = "Issue a verifiable credential to a user")
    public ResponseEntity<ApiResponse<CredentialResponse>> issueCredential(
            @Valid @RequestBody CredentialCreateRequest request) {
        CredentialResponse response = credentialService.issueCredential(request);
        return new ResponseEntity<>(ApiResponse.created("Credential issued successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/api/credentials")
    @Operation(summary = "Get all verifiable credentials")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> getAllCredentials() {
        List<CredentialResponse> credentials = credentialService.getAllCredentials();
        return ResponseEntity.ok(ApiResponse.success("Credentials retrieved successfully", credentials));
    }

    @GetMapping("/api/credentials/{id}")
    @Operation(summary = "Get credential by ID")
    public ResponseEntity<ApiResponse<CredentialResponse>> getCredentialById(@PathVariable Long id) {
        CredentialResponse credential = credentialService.getCredentialById(id);
        return ResponseEntity.ok(ApiResponse.success("Credential retrieved successfully", credential));
    }

    @GetMapping("/api/users/{userId}/credentials")
    @Operation(summary = "Get all credentials for a specific user")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> getCredentialsByUserId(@PathVariable Long userId) {
        List<CredentialResponse> credentials = credentialService.getCredentialsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User credentials retrieved successfully", credentials));
    }

    @PostMapping("/api/credentials/{id}/verify")
    @Operation(summary = "Cryptographically verify credential integrity, expiry, and active status")
    public ResponseEntity<ApiResponse<CredentialVerificationResponse>> verifyCredential(@PathVariable Long id) {
        CredentialVerificationResponse verification = credentialService.verifyCredential(id);
        return ResponseEntity.ok(ApiResponse.success("Credential verification completed", verification));
    }

    @PostMapping("/api/credentials/{id}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    @Operation(summary = "Revoke a verifiable credential (Admin/Faculty)")
    public ResponseEntity<ApiResponse<CredentialResponse>> revokeCredential(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        CredentialResponse response = credentialService.revokeCredential(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Credential revoked successfully", response));
    }

    @PutMapping("/api/credentials/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    @Operation(summary = "Update credential metadata")
    public ResponseEntity<ApiResponse<CredentialResponse>> updateCredential(
            @PathVariable Long id,
            @Valid @RequestBody CredentialUpdateRequest request) {
        CredentialResponse updated = credentialService.updateCredential(id, request);
        return ResponseEntity.ok(ApiResponse.success("Credential updated successfully", updated));
    }
}

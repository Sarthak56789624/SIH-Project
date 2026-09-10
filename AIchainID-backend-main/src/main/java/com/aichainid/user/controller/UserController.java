package com.aichainid.user.controller;

import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.asset.dto.AssetAssignmentResponse;
import com.aichainid.common.response.ApiResponse;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.user.dto.UserCreateRequest;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.dto.UserUpdateRequest;
import com.aichainid.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return new ResponseEntity<>(ApiResponse.created("User created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully"));
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "Get roles assigned to user")
    public ResponseEntity<ApiResponse<Set<String>>> getUserRoles(@PathVariable Long id) {
        Set<String> roles = userService.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success("User roles retrieved successfully", roles));
    }

    @GetMapping("/{id}/credentials")
    @Operation(summary = "Get credentials issued to user")
    public ResponseEntity<ApiResponse<List<CredentialResponse>>> getUserCredentials(@PathVariable Long id) {
        List<CredentialResponse> credentials = userService.getUserCredentials(id);
        return ResponseEntity.ok(ApiResponse.success("User credentials retrieved successfully", credentials));
    }

    @GetMapping("/{id}/assets")
    @Operation(summary = "Get assets assigned to user")
    public ResponseEntity<ApiResponse<List<AssetAssignmentResponse>>> getUserAssets(@PathVariable Long id) {
        List<AssetAssignmentResponse> assets = userService.getUserAssets(id);
        return ResponseEntity.ok(ApiResponse.success("User assets retrieved successfully", assets));
    }

    @GetMapping("/{id}/access-requests")
    @Operation(summary = "Get access requests created by user")
    public ResponseEntity<ApiResponse<List<AccessRequestResponse>>> getUserAccessRequests(@PathVariable Long id) {
        List<AccessRequestResponse> requests = userService.getUserAccessRequests(id);
        return ResponseEntity.ok(ApiResponse.success("User access requests retrieved successfully", requests));
    }
}

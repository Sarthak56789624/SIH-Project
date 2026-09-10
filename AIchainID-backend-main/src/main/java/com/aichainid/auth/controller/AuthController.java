package com.aichainid.auth.controller;

import com.aichainid.auth.dto.AuthResponse;
import com.aichainid.auth.dto.LoginRequest;
import com.aichainid.auth.dto.RegisterRequest;
import com.aichainid.auth.service.AuthService;
import com.aichainid.common.response.ApiResponse;
import com.aichainid.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and JWT login APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user under an organization")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.created("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password to receive JWT Bearer token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}

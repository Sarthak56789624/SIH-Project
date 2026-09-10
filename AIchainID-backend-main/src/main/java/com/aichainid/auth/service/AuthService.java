package com.aichainid.auth.service;

import com.aichainid.auth.dto.AuthResponse;
import com.aichainid.auth.dto.LoginRequest;
import com.aichainid.auth.dto.RegisterRequest;
import com.aichainid.user.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}

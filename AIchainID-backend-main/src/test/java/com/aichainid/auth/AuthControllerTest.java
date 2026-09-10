package com.aichainid.auth;

import com.aichainid.auth.controller.AuthController;
import com.aichainid.auth.dto.AuthResponse;
import com.aichainid.auth.dto.LoginRequest;
import com.aichainid.auth.dto.RegisterRequest;
import com.aichainid.auth.service.AuthService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.security.CustomUserDetailsService;
import com.aichainid.security.JwtService;
import com.aichainid.user.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should successfully register user")
    void testRegisterSuccess() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@vit.edu")
                .password("Password@123")
                .organizationId(1L)
                .build();

        UserResponse res = UserResponse.builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .email("john@vit.edu")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@vit.edu"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when registering with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@vit.edu")
                .password("Password@123")
                .organizationId(1L)
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User", "email", "existing@vit.edu"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("Should login successfully and return JWT response")
    void testLoginSuccess() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("john@vit.edu")
                .password("Password@123")
                .build();

        AuthResponse res = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400)
                .userId(10L)
                .email("john@vit.edu")
                .roles(List.of("ROLE_STUDENT"))
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for invalid credentials")
    void testLoginBadCredentials() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("john@vit.edu")
                .password("WrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }
}

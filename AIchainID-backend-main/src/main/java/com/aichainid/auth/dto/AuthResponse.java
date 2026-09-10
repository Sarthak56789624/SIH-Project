package com.aichainid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private Long organizationId;

    private String organizationName;

    private List<String> roles;
}

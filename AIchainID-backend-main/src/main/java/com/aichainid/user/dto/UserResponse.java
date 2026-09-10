package com.aichainid.user.dto;

import com.aichainid.user.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserStatus status;
    private Long organizationId;
    private String organizationName;
    private String did;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

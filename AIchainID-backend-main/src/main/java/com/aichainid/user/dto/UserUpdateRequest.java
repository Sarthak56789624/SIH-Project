package com.aichainid.user.dto;

import com.aichainid.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @Email(message = "Valid email is required")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    private Long organizationId;

    private UserStatus status;

    private Set<Long> roleIds;
}

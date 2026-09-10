package com.aichainid.organization.dto;

import com.aichainid.organization.entity.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreateRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    private String description;

    @NotNull(message = "Organization type is required")
    private OrganizationType organizationType;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phone;

    private String address;
}

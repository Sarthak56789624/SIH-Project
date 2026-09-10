package com.aichainid.organization.dto;

import com.aichainid.organization.entity.OrganizationStatus;
import com.aichainid.organization.entity.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUpdateRequest {

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    private String description;

    private OrganizationType organizationType;

    @Email(message = "Valid email is required")
    private String email;

    private String phone;

    private String address;

    private OrganizationStatus status;
}

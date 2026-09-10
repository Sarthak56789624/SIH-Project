package com.aichainid.role.dto;

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
public class RoleUpdateRequest {

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    private String description;

    private Long organizationId;

    private Set<Long> permissionIds;
}

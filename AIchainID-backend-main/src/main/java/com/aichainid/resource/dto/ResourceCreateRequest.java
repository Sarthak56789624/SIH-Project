package com.aichainid.resource.dto;

import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceStatus;
import com.aichainid.resource.entity.ResourceType;
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
public class ResourceCreateRequest {

    @NotBlank(message = "Resource name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    private String description;

    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;

    private String location;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private ResourceStatus status;

    @NotNull(message = "Sensitivity level is required")
    private ResourceSensitivityLevel sensitivityLevel;
}

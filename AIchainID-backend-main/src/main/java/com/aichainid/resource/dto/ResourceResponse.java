package com.aichainid.resource.dto;

import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceStatus;
import com.aichainid.resource.entity.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private ResourceType resourceType;
    private String location;
    private Long organizationId;
    private String organizationName;
    private ResourceStatus status;
    private ResourceSensitivityLevel sensitivityLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.aichainid.resource.mapper;

import com.aichainid.resource.dto.ResourceCreateRequest;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.resource.dto.ResourceUpdateRequest;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceStatus;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public Resource toEntity(ResourceCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Resource.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .resourceType(request.getResourceType())
                .location(request.getLocation())
                .status(request.getStatus() != null ? request.getStatus() : ResourceStatus.AVAILABLE)
                .sensitivityLevel(request.getSensitivityLevel() != null ? request.getSensitivityLevel() : ResourceSensitivityLevel.LOW)
                .build();
    }

    public ResourceResponse toResponse(Resource resource) {
        if (resource == null) {
            return null;
        }
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .resourceType(resource.getResourceType())
                .location(resource.getLocation())
                .organizationId(resource.getOrganization() != null ? resource.getOrganization().getId() : null)
                .organizationName(resource.getOrganization() != null ? resource.getOrganization().getName() : null)
                .status(resource.getStatus())
                .sensitivityLevel(resource.getSensitivityLevel())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    public void updateEntity(Resource resource, ResourceUpdateRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            resource.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getResourceType() != null) {
            resource.setResourceType(request.getResourceType());
        }
        if (request.getLocation() != null) {
            resource.setLocation(request.getLocation());
        }
        if (request.getStatus() != null) {
            resource.setStatus(request.getStatus());
        }
        if (request.getSensitivityLevel() != null) {
            resource.setSensitivityLevel(request.getSensitivityLevel());
        }
    }
}

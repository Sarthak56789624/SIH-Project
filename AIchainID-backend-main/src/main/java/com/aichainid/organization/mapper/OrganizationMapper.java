package com.aichainid.organization.mapper;

import com.aichainid.organization.dto.OrganizationCreateRequest;
import com.aichainid.organization.dto.OrganizationResponse;
import com.aichainid.organization.dto.OrganizationUpdateRequest;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.entity.OrganizationStatus;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toEntity(OrganizationCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Organization.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .organizationType(request.getOrganizationType())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(OrganizationStatus.ACTIVE)
                .build();
    }

    public OrganizationResponse toResponse(Organization organization) {
        if (organization == null) {
            return null;
        }
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .organizationType(organization.getOrganizationType())
                .email(organization.getEmail())
                .phone(organization.getPhone())
                .address(organization.getAddress())
                .status(organization.getStatus())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }

    public void updateEntity(Organization organization, OrganizationUpdateRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            organization.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getOrganizationType() != null) {
            organization.setOrganizationType(request.getOrganizationType());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            organization.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getPhone() != null) {
            organization.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            organization.setAddress(request.getAddress());
        }
        if (request.getStatus() != null) {
            organization.setStatus(request.getStatus());
        }
    }
}

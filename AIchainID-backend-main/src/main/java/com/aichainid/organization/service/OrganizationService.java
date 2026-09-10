package com.aichainid.organization.service;

import com.aichainid.asset.dto.AssetResponse;
import com.aichainid.organization.dto.OrganizationCreateRequest;
import com.aichainid.organization.dto.OrganizationResponse;
import com.aichainid.organization.dto.OrganizationUpdateRequest;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.user.dto.UserResponse;

import java.util.List;

public interface OrganizationService {

    OrganizationResponse createOrganization(OrganizationCreateRequest request);

    List<OrganizationResponse> getAllOrganizations();

    OrganizationResponse getOrganizationById(Long id);

    OrganizationResponse updateOrganization(Long id, OrganizationUpdateRequest request);

    void deleteOrganization(Long id);

    List<UserResponse> getOrganizationUsers(Long organizationId);

    List<ResourceResponse> getOrganizationResources(Long organizationId);

    List<AssetResponse> getOrganizationAssets(Long organizationId);
}

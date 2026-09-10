package com.aichainid.resource.service;

import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.resource.dto.ResourceCreateRequest;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.resource.dto.ResourceUpdateRequest;

import java.util.List;

public interface ResourceService {

    ResourceResponse createResource(ResourceCreateRequest request);

    List<ResourceResponse> getAllResources();

    ResourceResponse getResourceById(Long id);

    ResourceResponse updateResource(Long id, ResourceUpdateRequest request);

    void deleteResource(Long id);

    List<AccessRequestResponse> getResourceAccessRequests(Long id);
}

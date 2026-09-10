package com.aichainid.resource.service;

import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.access.mapper.AccessRequestMapper;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.resource.dto.ResourceCreateRequest;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.resource.dto.ResourceUpdateRequest;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceStatus;
import com.aichainid.resource.mapper.ResourceMapper;
import com.aichainid.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final OrganizationRepository organizationRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final ResourceMapper resourceMapper;
    private final AccessRequestMapper accessRequestMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceCreateRequest request) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        Resource resource = resourceMapper.toEntity(request);
        resource.setOrganization(organization);

        Resource saved = resourceRepository.save(resource);

        auditLogService.log(AuditEventType.RESOURCE_CREATED, "RESOURCE", saved.getId(),
                "Resource created: " + saved.getName() + " (" + saved.getResourceType() + ", Sensitivity: " + saved.getSensitivityLevel() + ")");

        return resourceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll().stream()
                .map(resourceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));
        return resourceMapper.toResponse(resource);
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceUpdateRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        resourceMapper.updateEntity(resource, request);

        if (request.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            resource.setOrganization(organization);
        }

        Resource updated = resourceRepository.save(resource);

        auditLogService.log(AuditEventType.RESOURCE_UPDATED, "RESOURCE", updated.getId(),
                "Resource updated: " + updated.getName());

        return resourceMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        resource.setStatus(ResourceStatus.OFFLINE);
        resourceRepository.save(resource);

        auditLogService.log(AuditEventType.RESOURCE_DELETED, "RESOURCE", id,
                "Resource marked offline: " + resource.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessRequestResponse> getResourceAccessRequests(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource", "id", id);
        }
        return accessRequestRepository.findByResourceId(id).stream()
                .map(accessRequestMapper::toResponse)
                .collect(Collectors.toList());
    }
}

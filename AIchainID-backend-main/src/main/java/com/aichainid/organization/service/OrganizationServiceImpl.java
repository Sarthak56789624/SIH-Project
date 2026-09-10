package com.aichainid.organization.service;

import com.aichainid.asset.dto.AssetResponse;
import com.aichainid.asset.mapper.AssetMapper;
import com.aichainid.asset.repository.AssetRepository;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.organization.dto.OrganizationCreateRequest;
import com.aichainid.organization.dto.OrganizationResponse;
import com.aichainid.organization.dto.OrganizationUpdateRequest;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.entity.OrganizationStatus;
import com.aichainid.organization.mapper.OrganizationMapper;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.resource.dto.ResourceResponse;
import com.aichainid.resource.mapper.ResourceMapper;
import com.aichainid.resource.repository.ResourceRepository;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.mapper.UserMapper;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final AssetRepository assetRepository;
    private final OrganizationMapper organizationMapper;
    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;
    private final AssetMapper assetMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationCreateRequest request) {
        if (organizationRepository.existsByName(request.getName().trim())) {
            throw new DuplicateResourceException("Organization", "name", request.getName());
        }

        Organization organization = organizationMapper.toEntity(request);
        Organization saved = organizationRepository.save(organization);

        auditLogService.log(AuditEventType.ORGANIZATION_CREATED, "ORGANIZATION", saved.getId(),
                "Organization created: " + saved.getName());

        return organizationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(organizationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));
        return organizationMapper.toResponse(org);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(Long id, OrganizationUpdateRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        if (request.getName() != null && !request.getName().trim().equalsIgnoreCase(organization.getName())) {
            if (organizationRepository.existsByName(request.getName().trim())) {
                throw new DuplicateResourceException("Organization", "name", request.getName());
            }
        }

        organizationMapper.updateEntity(organization, request);
        Organization updated = organizationRepository.save(organization);

        auditLogService.log(AuditEventType.ORGANIZATION_UPDATED, "ORGANIZATION", updated.getId(),
                "Organization updated: " + updated.getName());

        return organizationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", id));

        organization.setStatus(OrganizationStatus.INACTIVE);
        organizationRepository.save(organization);

        auditLogService.log(AuditEventType.ORGANIZATION_DELETED, "ORGANIZATION", id,
                "Organization deactivated: " + organization.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getOrganizationUsers(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        return userRepository.findByOrganizationId(organizationId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getOrganizationResources(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        return resourceRepository.findByOrganizationId(organizationId).stream()
                .map(resourceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getOrganizationAssets(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization", "id", organizationId);
        }
        return assetRepository.findByOrganizationId(organizationId).stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }
}

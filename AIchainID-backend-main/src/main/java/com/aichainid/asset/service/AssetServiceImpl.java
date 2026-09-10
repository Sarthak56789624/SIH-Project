package com.aichainid.asset.service;

import com.aichainid.asset.dto.*;
import com.aichainid.asset.entity.Asset;
import com.aichainid.asset.entity.AssetAssignment;
import com.aichainid.asset.entity.AssetAssignmentStatus;
import com.aichainid.asset.entity.AssetStatus;
import com.aichainid.asset.mapper.AssetMapper;
import com.aichainid.asset.repository.AssetAssignmentRepository;
import com.aichainid.asset.repository.AssetRepository;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.BadRequestException;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.security.SecurityUtils;
import com.aichainid.user.entity.User;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AssetMapper assetMapper;
    private final BlockchainService blockchainService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AssetResponse createAsset(AssetCreateRequest request) {
        String assetCode = request.getAssetCode().trim().toUpperCase();
        if (assetRepository.existsByAssetCode(assetCode)) {
            throw new DuplicateResourceException("Asset", "assetCode", assetCode);
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));

        Asset asset = assetMapper.toEntity(request);
        asset.setOrganization(organization);

        Asset saved = assetRepository.save(asset);

        auditLogService.log(AuditEventType.ASSET_CREATED, "ASSET", saved.getId(),
                "Asset created: " + saved.getName() + " (" + saved.getAssetCode() + ")");

        return assetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        return assetMapper.toResponse(asset);
    }

    @Override
    @Transactional
    public AssetResponse updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        if (request.getAssetCode() != null && !request.getAssetCode().trim().equalsIgnoreCase(asset.getAssetCode())) {
            String newCode = request.getAssetCode().trim().toUpperCase();
            if (assetRepository.existsByAssetCode(newCode)) {
                throw new DuplicateResourceException("Asset", "assetCode", newCode);
            }
        }

        assetMapper.updateEntity(asset, request);

        if (request.getOrganizationId() != null) {
            Organization organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId()));
            asset.setOrganization(organization);
        }

        Asset updated = assetRepository.save(asset);

        auditLogService.log(AuditEventType.ASSET_UPDATED, "ASSET", updated.getId(),
                "Asset updated: " + updated.getName() + " (" + updated.getAssetCode() + ")");

        return assetMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        asset.setStatus(AssetStatus.RETIRED);
        assetRepository.save(asset);

        auditLogService.log(AuditEventType.ASSET_UPDATED, "ASSET", id,
                "Asset marked retired: " + asset.getAssetCode());
    }

    @Override
    @Transactional
    public AssetAssignmentResponse assignAsset(Long assetId, AssetAssignmentRequest request) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", assetId));

        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException("Asset is already assigned. It must be returned before re-assigning.");
        }
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException("Asset is not available for assignment (Status: " + asset.getStatus() + ")");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Long assignedById = SecurityUtils.getCurrentUserId();
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedById));

        // Update asset status
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .user(user)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .status(AssetAssignmentStatus.ASSIGNED)
                .notes(request.getNotes())
                .build();

        AssetAssignment saved = assetAssignmentRepository.save(assignment);

        // Record on Blockchain Ledger
        String recordHash = CryptoUtils.sha256Hex("ASSET_ASSIGNED:" + assetId + ":" + user.getId() + ":" + System.currentTimeMillis());
        blockchainService.recordAssetAssignment(saved.getId(), recordHash);

        // Audit Log
        auditLogService.log(user.getId(), AuditEventType.ASSET_ASSIGNED, "ASSET", assetId,
                "Assigned asset " + asset.getAssetCode() + " (" + asset.getName() + ") to user " + user.getEmail() + " by " + assignedBy.getEmail());

        return assetMapper.toAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public AssetAssignmentResponse returnAsset(Long assetId, String notes) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", assetId));

        AssetAssignment activeAssignment = assetAssignmentRepository.findByAssetIdAndStatus(assetId, AssetAssignmentStatus.ASSIGNED)
                .orElseThrow(() -> new BadRequestException("No active assignment found for asset ID: " + assetId));

        activeAssignment.setStatus(AssetAssignmentStatus.RETURNED);
        activeAssignment.setReturnedAt(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            activeAssignment.setNotes((activeAssignment.getNotes() != null ? activeAssignment.getNotes() + " | Return notes: " : "") + notes);
        }

        AssetAssignment updatedAssignment = assetAssignmentRepository.save(activeAssignment);

        // Make asset available again
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        // Record on Blockchain Ledger
        String recordHash = CryptoUtils.sha256Hex("ASSET_RETURNED:" + assetId + ":" + activeAssignment.getUser().getId() + ":" + System.currentTimeMillis());
        blockchainService.recordAssetReturn(updatedAssignment.getId(), recordHash);

        // Audit Log
        auditLogService.log(activeAssignment.getUser().getId(), AuditEventType.ASSET_RETURNED, "ASSET", assetId,
                "Returned asset " + asset.getAssetCode() + " from user " + activeAssignment.getUser().getEmail());

        return assetMapper.toAssignmentResponse(updatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAssignmentResponse> getAssetHistory(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }
        return assetAssignmentRepository.findByAssetId(assetId).stream()
                .map(assetMapper::toAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAssignmentResponse> getAssetsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return assetAssignmentRepository.findByUserId(userId).stream()
                .map(assetMapper::toAssignmentResponse)
                .collect(Collectors.toList());
    }
}

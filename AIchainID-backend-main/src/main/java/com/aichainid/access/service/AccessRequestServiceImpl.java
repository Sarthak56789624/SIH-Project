package com.aichainid.access.service;

import com.aichainid.access.dto.AccessRequestCreateRequest;
import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.access.mapper.AccessRequestMapper;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.approval.dto.ApprovalDecisionRequest;
import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.approval.entity.ApprovalDecision;
import com.aichainid.approval.service.ApprovalService;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.BadRequestException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceStatus;
import com.aichainid.resource.repository.ResourceRepository;
import com.aichainid.risk.dto.RiskAssessmentResponse;
import com.aichainid.risk.service.RiskEngineService;
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
public class AccessRequestServiceImpl implements AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final RiskEngineService riskEngineService;
    private final ApprovalService approvalService;
    private final AccessRequestMapper accessRequestMapper;
    private final BlockchainService blockchainService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AccessRequestResponse createAccessRequest(AccessRequestCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.getResourceId()));

        if (resource.getStatus() == ResourceStatus.OFFLINE) {
            throw new BadRequestException("Resource is offline and cannot be requested");
        }

        if (request.getRequestedUntil().isBefore(request.getRequestedFrom())) {
            throw new BadRequestException("Requested until time must be after requested start time");
        }

        AccessRequest accessRequest = accessRequestMapper.toEntity(request);
        accessRequest.setRequester(requester);
        accessRequest.setResource(resource);
        accessRequest.setStatus(AccessRequestStatus.PENDING);

        AccessRequest saved = accessRequestRepository.save(accessRequest);

        // Automatically run AI Risk Engine
        try {
            riskEngineService.evaluateAndSaveRisk(saved.getId());
        } catch (Exception e) {
            log.error("Automatic AI risk evaluation encountered an error: {}", e.getMessage());
        }

        auditLogService.log(userId, AuditEventType.ACCESS_REQUESTED, "ACCESS_REQUEST", saved.getId(),
                "Access requested for resource: " + resource.getName() + " by " + requester.getEmail());

        return accessRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessRequestResponse> getAllAccessRequests() {
        return accessRequestRepository.findAll().stream()
                .map(accessRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccessRequestResponse getAccessRequestById(Long id) {
        AccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", id));
        return accessRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccessRequestResponse> getAccessRequestsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return accessRequestRepository.findByRequesterId(userId).stream()
                .map(accessRequestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccessRequestResponse cancelAccessRequest(Long id) {
        AccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", id));

        if (request.getStatus() != AccessRequestStatus.PENDING && request.getStatus() != AccessRequestStatus.ADMIN_REVIEW) {
            throw new BadRequestException("Only pending or under-review access requests can be cancelled");
        }

        request.setStatus(AccessRequestStatus.CANCELLED);
        AccessRequest updated = accessRequestRepository.save(request);

        auditLogService.log(request.getRequester().getId(), AuditEventType.ACCESS_CANCELLED, "ACCESS_REQUEST", id,
                "Cancelled access request for " + request.getResource().getName());

        return accessRequestMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public RiskAssessmentResponse evaluateRisk(Long id) {
        return riskEngineService.evaluateAndSaveRisk(id);
    }

    @Override
    @Transactional
    public ApprovalResponse approveAccessRequest(Long id, ApprovalDecisionRequest request) {
        return approvalService.recordDecision(id, ApprovalDecision.APPROVED, request);
    }

    @Override
    @Transactional
    public ApprovalResponse breakGlassAccessRequest(Long id, ApprovalDecisionRequest request) {
        return approvalService.recordBreakGlassDecision(id, request);
    }

    @Override
    @Transactional
    public ApprovalResponse rejectAccessRequest(Long id, ApprovalDecisionRequest request) {
        return approvalService.recordDecision(id, ApprovalDecision.REJECTED, request);
    }

    @Override
    @Transactional
    public AccessRequestResponse revokeAccessRequest(Long id, ApprovalDecisionRequest request) {
        AccessRequest accessRequest = accessRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", id));

        if (accessRequest.getStatus() != AccessRequestStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED access requests can be revoked");
        }

        accessRequest.setStatus(AccessRequestStatus.REVOKED);
        AccessRequest updated = accessRequestRepository.save(accessRequest);

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String reason = request != null ? request.getReason() : "Access revoked by admin";

        // Record on blockchain
        String recordHash = CryptoUtils.sha256Hex("REVOKED:" + id + ":" + currentUserId + ":" + System.currentTimeMillis());
        blockchainService.recordAccessRevocation(id, recordHash);

        // Audit
        auditLogService.log(currentUserId, AuditEventType.ACCESS_REVOKED, "ACCESS_REQUEST", id,
                "Revoked access request " + id + " for resource " + accessRequest.getResource().getName() + ". Reason: " + reason);

        return accessRequestMapper.toResponse(updated);
    }
}

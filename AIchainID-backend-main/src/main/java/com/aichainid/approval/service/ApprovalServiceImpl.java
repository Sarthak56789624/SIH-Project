package com.aichainid.approval.service;

import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.approval.dto.ApprovalDecisionRequest;
import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.approval.entity.Approval;
import com.aichainid.approval.entity.ApprovalDecision;
import com.aichainid.approval.mapper.ApprovalMapper;
import com.aichainid.approval.repository.ApprovalRepository;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.BadRequestException;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.security.SecurityUtils;
import com.aichainid.risk.entity.RiskAssessment;
import com.aichainid.risk.repository.RiskAssessmentRepository;
import com.aichainid.user.entity.User;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final UserRepository userRepository;
    private final ApprovalMapper approvalMapper;
    private final BlockchainService blockchainService;
    private final AuditLogService auditLogService;
    private final RiskAssessmentRepository riskAssessmentRepository;

    @Override
    @Transactional
    public ApprovalResponse recordDecision(Long accessRequestId, ApprovalDecision decision, ApprovalDecisionRequest request) {
        AccessRequest accessRequest = accessRequestRepository.findById(accessRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", accessRequestId));

        Long adminId = SecurityUtils.getCurrentUserId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        // Business Rule 1: Requester cannot approve their own request unless explicitly authorized
        if (accessRequest.getRequester().getId().equals(adminId) && !SecurityUtils.hasRole("ADMIN")) {
            throw new BadRequestException("Self-approval is forbidden: You cannot approve your own access request");
        }

        // ZERO-TRUST AI POLICY GUARDRAIL:
        // If AI evaluates risk as HIGH or CRITICAL (> 70), standard unilateral admin approval is blocked!
        if (decision == ApprovalDecision.APPROVED) {
            Optional<RiskAssessment> riskOpt = riskAssessmentRepository.findByAccessRequestId(accessRequestId);
            if (riskOpt.isPresent()) {
                double riskScore = riskOpt.get().getRiskScore();
                if (riskScore > 70.0) {
                    log.warn("ZERO-TRUST SMART POLICY BLOCKED: Request #{} has risk score {} > 70. Standard approval locked!",
                            accessRequestId, riskScore);
                    throw new BadRequestException("SMART CONTRACT POLICY VIOLATION: Standard approval is LOCKED because AI Risk Score is " 
                            + riskScore + "/100 (HIGH/CRITICAL). To grant emergency access, invoke the BREAK-GLASS Protocol with mandatory justification.");
                }
            }
        }

        String reason = request != null ? request.getReason() : null;

        Approval approval = Approval.builder()
                .accessRequest(accessRequest)
                .admin(admin)
                .decision(decision)
                .reason(reason)
                .isBreakGlass(false)
                .approvedAt(LocalDateTime.now())
                .build();

        Approval saved = approvalRepository.save(approval);

        // Update Access Request State
        if (decision == ApprovalDecision.APPROVED) {
            accessRequest.setStatus(AccessRequestStatus.APPROVED);
            accessRequestRepository.save(accessRequest);

            // Record on Blockchain
            String recordHash = CryptoUtils.sha256Hex("APPROVED:" + accessRequestId + ":" + adminId + ":" + System.currentTimeMillis());
            blockchainService.recordAccessApproval(accessRequestId, recordHash);

            // Audit
            auditLogService.log(adminId, AuditEventType.ACCESS_APPROVED, "ACCESS_REQUEST", accessRequestId,
                    "Admin " + admin.getEmail() + " APPROVED access request " + accessRequestId + " for resource " + accessRequest.getResource().getName());
        } else {
            accessRequest.setStatus(AccessRequestStatus.REJECTED);
            accessRequestRepository.save(accessRequest);

            // Record on Blockchain
            String recordHash = CryptoUtils.sha256Hex("REJECTED:" + accessRequestId + ":" + adminId + ":" + System.currentTimeMillis());
            blockchainService.recordAccessRejection(accessRequestId, recordHash);

            // Audit
            auditLogService.log(adminId, AuditEventType.ACCESS_REJECTED, "ACCESS_REQUEST", accessRequestId,
                    "Admin " + admin.getEmail() + " REJECTED access request " + accessRequestId + " for resource " + accessRequest.getResource().getName());
        }

        return approvalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalResponse recordBreakGlassDecision(Long accessRequestId, ApprovalDecisionRequest request) {
        AccessRequest accessRequest = accessRequestRepository.findById(accessRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", accessRequestId));

        Long adminId = SecurityUtils.getCurrentUserId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        if (request == null || (request.getEmergencyReason() == null && request.getReason() == null)) {
            throw new BadRequestException("Mandatory justification required for Break-Glass emergency override.");
        }

        String justification = request.getEmergencyReason() != null ? request.getEmergencyReason() : request.getReason();
        if (justification.trim().length() < 10) {
            throw new BadRequestException("Break-Glass justification must be at least 10 characters detailing the operational emergency.");
        }

        // 1. Record Break-Glass Approval in DB
        Approval approval = Approval.builder()
                .accessRequest(accessRequest)
                .admin(admin)
                .decision(ApprovalDecision.APPROVED)
                .reason("[BREAK-GLASS EMERGENCY OVERRIDE]: " + justification)
                .isBreakGlass(true)
                .approvedAt(LocalDateTime.now())
                .build();

        Approval saved = approvalRepository.save(approval);

        // 2. Set Access Request Status to APPROVED with time window
        accessRequest.setStatus(AccessRequestStatus.APPROVED);
        // Emergency access expires in 2 hours
        accessRequest.setRequestedUntil(LocalDateTime.now().plusHours(2));
        accessRequestRepository.save(accessRequest);

        // 3. Anchor permanently on Blockchain Ledger with BREAK_GLASS_TRIGGERED event
        String recordHash = CryptoUtils.sha256Hex("BREAK_GLASS:" + accessRequestId + ":" + adminId + ":" + justification);
        blockchainService.recordBreakGlassApproval(accessRequestId, justification, adminId, recordHash);

        // 4. Audit Log with Emergency Alert level
        auditLogService.log(adminId, AuditEventType.ACCESS_APPROVED, "ACCESS_REQUEST", accessRequestId,
                "🚨 [BREAK-GLASS INVOKED] Admin " + admin.getEmail() + " executed emergency bypass for request #" 
                + accessRequestId + " (" + accessRequest.getResource().getName() + "). Reason: " + justification);

        log.warn("🚨 BREAK-GLASS PROTOCOL: Emergency override executed for request #{} by admin #{}. Access window: 2 hours.",
                accessRequestId, adminId);

        return approvalMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getApprovalsByAccessRequestId(Long accessRequestId) {
        return approvalRepository.findByAccessRequestId(accessRequestId).stream()
                .map(approvalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getApprovalsByAdminId(Long adminId) {
        return approvalRepository.findByAdminId(adminId).stream()
                .map(approvalMapper::toResponse)
                .collect(Collectors.toList());
    }
}

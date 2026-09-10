package com.aichainid.access.mapper;

import com.aichainid.access.dto.AccessRequestCreateRequest;
import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.approval.mapper.ApprovalMapper;
import com.aichainid.approval.repository.ApprovalRepository;
import com.aichainid.risk.mapper.RiskAssessmentMapper;
import com.aichainid.risk.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessRequestMapper {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskAssessmentMapper riskAssessmentMapper;
    private final ApprovalRepository approvalRepository;
    private final ApprovalMapper approvalMapper;

    public AccessRequest toEntity(AccessRequestCreateRequest request) {
        if (request == null) {
            return null;
        }
        return AccessRequest.builder()
                .reason(request.getReason().trim())
                .requestedFrom(request.getRequestedFrom())
                .requestedUntil(request.getRequestedUntil())
                .status(AccessRequestStatus.PENDING)
                .build();
    }

    public AccessRequestResponse toResponse(AccessRequest request) {
        if (request == null) {
            return null;
        }

        var risk = riskAssessmentRepository.findByAccessRequestId(request.getId())
                .map(riskAssessmentMapper::toResponse)
                .orElse(null);

        var approval = approvalRepository.findTopByAccessRequestIdOrderByCreatedAtDesc(request.getId())
                .map(approvalMapper::toResponse)
                .orElse(null);

        return AccessRequestResponse.builder()
                .id(request.getId())
                .requesterId(request.getRequester() != null ? request.getRequester().getId() : null)
                .requesterEmail(request.getRequester() != null ? request.getRequester().getEmail() : null)
                .requesterName(request.getRequester() != null ? request.getRequester().getFirstName() + " " + request.getRequester().getLastName() : null)
                .resourceId(request.getResource() != null ? request.getResource().getId() : null)
                .resourceName(request.getResource() != null ? request.getResource().getName() : null)
                .resourceType(request.getResource() != null ? request.getResource().getResourceType() : null)
                .resourceSensitivityLevel(request.getResource() != null ? request.getResource().getSensitivityLevel() : null)
                .reason(request.getReason())
                .requestedFrom(request.getRequestedFrom())
                .requestedUntil(request.getRequestedUntil())
                .status(request.getStatus())
                .riskAssessment(risk)
                .approval(approval)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}

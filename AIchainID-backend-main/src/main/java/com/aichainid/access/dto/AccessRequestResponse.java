package com.aichainid.access.dto;

import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceType;
import com.aichainid.risk.dto.RiskAssessmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestResponse {

    private Long id;
    private Long requesterId;
    private String requesterEmail;
    private String requesterName;
    private Long resourceId;
    private String resourceName;
    private ResourceType resourceType;
    private ResourceSensitivityLevel resourceSensitivityLevel;
    private String reason;
    private LocalDateTime requestedFrom;
    private LocalDateTime requestedUntil;
    private AccessRequestStatus status;
    private RiskAssessmentResponse riskAssessment;
    private ApprovalResponse approval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

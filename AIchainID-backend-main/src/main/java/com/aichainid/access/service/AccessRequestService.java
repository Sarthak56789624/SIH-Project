package com.aichainid.access.service;

import com.aichainid.access.dto.AccessRequestCreateRequest;
import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.approval.dto.ApprovalDecisionRequest;
import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.risk.dto.RiskAssessmentResponse;

import java.util.List;

public interface AccessRequestService {

    AccessRequestResponse createAccessRequest(AccessRequestCreateRequest request);

    List<AccessRequestResponse> getAllAccessRequests();

    AccessRequestResponse getAccessRequestById(Long id);

    List<AccessRequestResponse> getAccessRequestsByUserId(Long userId);

    AccessRequestResponse cancelAccessRequest(Long id);

    RiskAssessmentResponse evaluateRisk(Long id);

    ApprovalResponse approveAccessRequest(Long id, ApprovalDecisionRequest request);

    ApprovalResponse breakGlassAccessRequest(Long id, ApprovalDecisionRequest request);

    ApprovalResponse rejectAccessRequest(Long id, ApprovalDecisionRequest request);

    AccessRequestResponse revokeAccessRequest(Long id, ApprovalDecisionRequest request);
}

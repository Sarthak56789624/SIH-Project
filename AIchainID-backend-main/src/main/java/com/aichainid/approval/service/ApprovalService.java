package com.aichainid.approval.service;

import com.aichainid.approval.dto.ApprovalDecisionRequest;
import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.approval.entity.ApprovalDecision;

import java.util.List;

public interface ApprovalService {

    ApprovalResponse recordDecision(Long accessRequestId, ApprovalDecision decision, ApprovalDecisionRequest request);

    ApprovalResponse recordBreakGlassDecision(Long accessRequestId, ApprovalDecisionRequest request);

    List<ApprovalResponse> getApprovalsByAccessRequestId(Long accessRequestId);

    List<ApprovalResponse> getApprovalsByAdminId(Long adminId);
}

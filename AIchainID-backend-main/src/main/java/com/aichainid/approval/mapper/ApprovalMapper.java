package com.aichainid.approval.mapper;

import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.approval.entity.Approval;
import org.springframework.stereotype.Component;

@Component
public class ApprovalMapper {

    public ApprovalResponse toResponse(Approval approval) {
        if (approval == null) {
            return null;
        }
        return ApprovalResponse.builder()
                .id(approval.getId())
                .accessRequestId(approval.getAccessRequest() != null ? approval.getAccessRequest().getId() : null)
                .adminId(approval.getAdmin() != null ? approval.getAdmin().getId() : null)
                .adminEmail(approval.getAdmin() != null ? approval.getAdmin().getEmail() : null)
                .adminName(approval.getAdmin() != null ? approval.getAdmin().getFirstName() + " " + approval.getAdmin().getLastName() : null)
                .decision(approval.getDecision())
                .reason(approval.getReason())
                .isBreakGlass(approval.getIsBreakGlass())
                .approvedAt(approval.getApprovedAt())
                .createdAt(approval.getCreatedAt())
                .build();
    }
}

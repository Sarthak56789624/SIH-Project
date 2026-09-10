package com.aichainid.approval.dto;

import com.aichainid.approval.entity.ApprovalDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {

    private Long id;
    private Long accessRequestId;
    private Long adminId;
    private String adminEmail;
    private String adminName;
    private ApprovalDecision decision;
    private String reason;
    private Boolean isBreakGlass;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}

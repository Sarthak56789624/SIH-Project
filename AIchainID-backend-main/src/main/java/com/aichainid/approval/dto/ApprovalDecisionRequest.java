package com.aichainid.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDecisionRequest {

    private String reason;
    private Boolean isBreakGlass;
    private String emergencyReason;
}

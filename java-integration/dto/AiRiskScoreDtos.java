package com.aichainid.ai.dto;

import java.util.List;

public class AiRiskScoreDtos {

    // ---- Request: matches Python RiskRequest exactly (field names matter) ----
    public record AiRiskScoreRequest(
            String did,
            String role,
            String department,      // nullable
            String resource,
            String time,             // "HH:MM", 24h, e.g. "02:30"
            boolean deviceKnown,
            boolean locationMatch,
            int failedAttempts,
            double requestFrequency,
            boolean credentialValid,
            double previousBehaviorScore
    ) {}

    // ---- Response: matches Python RiskResponse exactly ----
    public record AiRiskScoreResponse(
            double riskScore,
            String riskLevel,        // LOW / MEDIUM / HIGH
            String decision,         // ALLOW / ADMIN_REVIEW / BLOCK
            List<String> reasons,
            double modelConfidence
    ) {}
}

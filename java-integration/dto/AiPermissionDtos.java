package com.aichainid.ai.dto;

import java.util.List;

public class AiPermissionDtos {

    // ---- Request: matches Python PermissionRequest exactly ----
    public record AiPermissionRequest(
            String role,
            String department,
            String employmentType,          // default "Employee" if you don't have this concept yet
            List<String> credentials,
            List<String> projects,
            List<String> resources,
            int contractDurationDays,
            boolean previouslyApprovedSimilar
    ) {}

    // ---- Response: matches Python PermissionResponse exactly ----
    public record AiPermissionItem(
            String resource,
            String recommendation,   // ALLOW / DENY / REVIEW
            double confidence,
            String reason,
            String suggestedDuration // nullable, e.g. "30 days"
    ) {}

    public record AiPermissionResponse(
            List<AiPermissionItem> recommendations,
            String note
    ) {}
}

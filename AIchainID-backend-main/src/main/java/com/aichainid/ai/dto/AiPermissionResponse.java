package com.aichainid.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPermissionResponse {
    private List<RecommendationItem> recommendations;
    private String note;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        private String resource;
        private String recommendation;
        private double confidence;
        private String reason;
        private String suggestedDuration;
    }
}

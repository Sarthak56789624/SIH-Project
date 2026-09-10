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
public class AiRiskResponse {
    private double riskScore;
    private String riskLevel;
    private String decision;
    private List<String> reasons;
    private double modelConfidence;
}

package com.aichainid.risk.dto;

import com.aichainid.risk.entity.RiskLevel;
import com.aichainid.risk.entity.RiskRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResponse {

    private Long id;
    private Long accessRequestId;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private RiskRecommendation recommendation;
    private List<String> riskFactors;
    private String modelVersion;
    private LocalDateTime analyzedAt;
}

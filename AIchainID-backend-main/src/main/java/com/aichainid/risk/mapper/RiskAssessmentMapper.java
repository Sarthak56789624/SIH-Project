package com.aichainid.risk.mapper;

import com.aichainid.risk.dto.RiskAssessmentResponse;
import com.aichainid.risk.entity.RiskAssessment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class RiskAssessmentMapper {

    public RiskAssessmentResponse toResponse(RiskAssessment assessment) {
        if (assessment == null) {
            return null;
        }

        List<String> factors = Collections.emptyList();
        if (assessment.getRiskFactors() != null && !assessment.getRiskFactors().isBlank()) {
            factors = Arrays.asList(assessment.getRiskFactors().split(","));
        }

        return RiskAssessmentResponse.builder()
                .id(assessment.getId())
                .accessRequestId(assessment.getAccessRequest() != null ? assessment.getAccessRequest().getId() : null)
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .recommendation(assessment.getRecommendation())
                .riskFactors(factors)
                .modelVersion(assessment.getModelVersion())
                .analyzedAt(assessment.getAnalyzedAt())
                .build();
    }
}

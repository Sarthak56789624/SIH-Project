package com.aichainid.risk.service;

import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.audit.entity.AuditEventType;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.repository.CredentialRepository;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.risk.dto.RiskAssessmentResponse;
import com.aichainid.risk.entity.RiskAssessment;
import com.aichainid.risk.entity.RiskFactor;
import com.aichainid.risk.entity.RiskLevel;
import com.aichainid.risk.entity.RiskRecommendation;
import com.aichainid.risk.mapper.RiskAssessmentMapper;
import com.aichainid.risk.repository.RiskAssessmentRepository;
import com.aichainid.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleBasedRiskEngineService implements RiskEngineService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final CredentialRepository credentialRepository;
    private final RiskAssessmentMapper riskAssessmentMapper;
    private final AuditLogService auditLogService;

    @Override
    public RiskAssessment evaluateRisk(AccessRequest request) {
        User user = request.getRequester();
        Resource resource = request.getResource();

        int score = 0;
        List<String> riskFactors = new ArrayList<>();

        // 1. Resource Sensitivity Impact
        if (resource.getSensitivityLevel() == ResourceSensitivityLevel.CRITICAL) {
            score += 50;
            riskFactors.add(RiskFactor.HIGH_RESOURCE_SENSITIVITY.name());
        } else if (resource.getSensitivityLevel() == ResourceSensitivityLevel.HIGH) {
            score += 35;
            riskFactors.add(RiskFactor.HIGH_RESOURCE_SENSITIVITY.name());
        } else if (resource.getSensitivityLevel() == ResourceSensitivityLevel.MEDIUM) {
            score += 20;
        } else {
            score += 10;
        }

        // 2. Time-based Anomaly Analysis (Night hours 20:00 to 07:00 or weekends)
        int startHour = request.getRequestedFrom().getHour();
        int endHour = request.getRequestedUntil().getHour();
        DayOfWeek day = request.getRequestedFrom().getDayOfWeek();

        boolean isOffHours = (startHour >= 20 || startHour < 7) || (endHour >= 20 || endHour < 7);
        boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);

        if (isOffHours || isWeekend) {
            score += 25;
            riskFactors.add(RiskFactor.UNUSUAL_TIME.name());
        }

        // 3. User Role Privileges
        boolean isStudent = user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase("STUDENT"));
        boolean isFacultyOrAdmin = user.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase("ADMIN") || r.getName().equalsIgnoreCase("FACULTY"));

        if (isStudent && (resource.getSensitivityLevel() == ResourceSensitivityLevel.CRITICAL
                || resource.getSensitivityLevel() == ResourceSensitivityLevel.HIGH)) {
            score += 25;
            riskFactors.add(RiskFactor.INSUFFICIENT_ROLE_PRIVILEGE.name());
        }

        if (isFacultyOrAdmin) {
            score = Math.max(0, score - 20);
        }

        // 4. Credential Possession Check (Checks if user has any active credential)
        long activeCredentials = credentialRepository.findByUserIdAndStatus(user.getId(), CredentialStatus.ACTIVE).size();
        if (activeCredentials == 0 && (resource.getSensitivityLevel() == ResourceSensitivityLevel.HIGH
                || resource.getSensitivityLevel() == ResourceSensitivityLevel.CRITICAL)) {
            score += 25;
            riskFactors.add(RiskFactor.NO_VALID_CREDENTIAL.name());
        }

        // 5. Request Frequency (Abnormal burst requests in last 24 hours)
        long recentRequests = accessRequestRepository.countByRequesterIdAndCreatedAtAfter(
                user.getId(), LocalDateTime.now().minusHours(24));
        if (recentRequests >= 5) {
            score += 20;
            riskFactors.add(RiskFactor.EXCESSIVE_ACCESS.name());
        }

        // Clamp final score between 0 and 100
        int finalScore = Math.min(100, Math.max(0, score));

        // Determine Risk Level & Recommendation
        RiskLevel level;
        RiskRecommendation recommendation;

        if (finalScore <= 30) {
            level = RiskLevel.LOW;
            recommendation = RiskRecommendation.APPROVE;
        } else if (finalScore <= 65) {
            level = RiskLevel.MEDIUM;
            recommendation = RiskRecommendation.REVIEW;
        } else if (finalScore <= 85) {
            level = RiskLevel.HIGH;
            recommendation = RiskRecommendation.REVIEW;
        } else {
            level = RiskLevel.CRITICAL;
            recommendation = RiskRecommendation.REJECT;
        }

        return RiskAssessment.builder()
                .accessRequest(request)
                .riskScore(finalScore)
                .riskLevel(level)
                .recommendation(recommendation)
                .riskFactors(String.join(",", riskFactors))
                .modelVersion("v1.0-rule-engine")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public RiskAssessmentResponse evaluateAndSaveRisk(Long accessRequestId) {
        AccessRequest request = accessRequestRepository.findById(accessRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRequest", "id", accessRequestId));

        RiskAssessment calculated = evaluateRisk(request);

        RiskAssessment assessment = riskAssessmentRepository.findByAccessRequestId(accessRequestId)
                .orElseGet(() -> RiskAssessment.builder().accessRequest(request).build());

        assessment.setRiskScore(calculated.getRiskScore());
        assessment.setRiskLevel(calculated.getRiskLevel());
        assessment.setRecommendation(calculated.getRecommendation());
        assessment.setRiskFactors(calculated.getRiskFactors());
        assessment.setModelVersion(calculated.getModelVersion());
        assessment.setAnalyzedAt(LocalDateTime.now());

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        // Update request status to AI_REVIEW or ADMIN_REVIEW
        if (request.getStatus() == AccessRequestStatus.PENDING) {
            request.setStatus(AccessRequestStatus.ADMIN_REVIEW);
            accessRequestRepository.save(request);
        }

        auditLogService.log(request.getRequester().getId(), AuditEventType.RISK_ANALYZED, "ACCESS_REQUEST", accessRequestId,
                "AI Risk Engine evaluated request " + accessRequestId + " - Score: " + saved.getRiskScore() +
                        ", Level: " + saved.getRiskLevel() + ", Recommendation: " + saved.getRecommendation());

        return riskAssessmentMapper.toResponse(saved);
    }
}

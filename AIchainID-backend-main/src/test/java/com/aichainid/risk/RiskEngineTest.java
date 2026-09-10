package com.aichainid.risk;

import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.credential.entity.Credential;
import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.repository.CredentialRepository;
import com.aichainid.organization.entity.Organization;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceType;
import com.aichainid.risk.entity.RiskAssessment;
import com.aichainid.risk.entity.RiskLevel;
import com.aichainid.risk.entity.RiskRecommendation;
import com.aichainid.risk.mapper.RiskAssessmentMapper;
import com.aichainid.risk.repository.RiskAssessmentRepository;
import com.aichainid.risk.service.RuleBasedRiskEngineService;
import com.aichainid.role.entity.Role;
import com.aichainid.user.entity.User;
import com.aichainid.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskEngineTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private AccessRequestRepository accessRequestRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private RiskAssessmentMapper riskAssessmentMapper = new RiskAssessmentMapper();

    @InjectMocks
    private RuleBasedRiskEngineService riskEngineService;

    private User studentUser;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = Organization.builder().id(1L).name("Test University").build();
        Role studentRole = Role.builder().id(1L).name("STUDENT").build();

        studentUser = User.builder()
                .id(10L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.edu")
                .status(UserStatus.ACTIVE)
                .organization(organization)
                .roles(Set.of(studentRole))
                .build();
    }

    @Test
    @DisplayName("Low Risk: Student requesting low sensitivity resource during normal working hours with valid credentials")
    void testLowRiskScenario() {
        Resource computerLab = Resource.builder()
                .id(1L)
                .name("General Computer Lab")
                .resourceType(ResourceType.COMPUTER_LAB)
                .sensitivityLevel(ResourceSensitivityLevel.LOW)
                .build();

        // 10:00 to 14:00 on a Wednesday
        LocalDateTime from = LocalDateTime.of(2026, 9, 2, 10, 0);
        LocalDateTime until = LocalDateTime.of(2026, 9, 2, 14, 0);

        AccessRequest request = AccessRequest.builder()
                .id(100L)
                .requester(studentUser)
                .resource(computerLab)
                .reason("Study session")
                .requestedFrom(from)
                .requestedUntil(until)
                .status(AccessRequestStatus.PENDING)
                .build();

        when(credentialRepository.findByUserIdAndStatus(eq(studentUser.getId()), eq(CredentialStatus.ACTIVE)))
                .thenReturn(List.of(Credential.builder().id(1L).build()));
        when(accessRequestRepository.countByRequesterIdAndCreatedAtAfter(eq(studentUser.getId()), any(LocalDateTime.class)))
                .thenReturn(0L);

        RiskAssessment assessment = riskEngineService.evaluateRisk(request);

        assertNotNull(assessment);
        assertEquals(RiskLevel.LOW, assessment.getRiskLevel());
        assertEquals(RiskRecommendation.APPROVE, assessment.getRecommendation());
        assertTrue(assessment.getRiskScore() <= 30);
    }

    @Test
    @DisplayName("High/Critical Risk: Student requesting Critical GPU cluster at midnight with no credentials")
    void testCriticalRiskScenario() {
        Resource gpuServer = Resource.builder()
                .id(2L)
                .name("GPU Supercluster")
                .resourceType(ResourceType.GPU_SERVER)
                .sensitivityLevel(ResourceSensitivityLevel.CRITICAL)
                .build();

        // 02:00 to 05:00 at night
        LocalDateTime from = LocalDateTime.of(2026, 9, 2, 2, 0);
        LocalDateTime until = LocalDateTime.of(2026, 9, 2, 5, 0);

        AccessRequest request = AccessRequest.builder()
                .id(101L)
                .requester(studentUser)
                .resource(gpuServer)
                .reason("Personal testing")
                .requestedFrom(from)
                .requestedUntil(until)
                .status(AccessRequestStatus.PENDING)
                .build();

        when(credentialRepository.findByUserIdAndStatus(eq(studentUser.getId()), eq(CredentialStatus.ACTIVE)))
                .thenReturn(Collections.emptyList());
        when(accessRequestRepository.countByRequesterIdAndCreatedAtAfter(eq(studentUser.getId()), any(LocalDateTime.class)))
                .thenReturn(6L);

        RiskAssessment assessment = riskEngineService.evaluateRisk(request);

        assertNotNull(assessment);
        assertEquals(RiskLevel.CRITICAL, assessment.getRiskLevel());
        assertEquals(RiskRecommendation.REJECT, assessment.getRecommendation());
        assertTrue(assessment.getRiskScore() >= 85);
        assertTrue(assessment.getRiskFactors().contains("HIGH_RESOURCE_SENSITIVITY"));
        assertTrue(assessment.getRiskFactors().contains("UNUSUAL_TIME"));
        assertTrue(assessment.getRiskFactors().contains("NO_VALID_CREDENTIAL"));
        assertTrue(assessment.getRiskFactors().contains("EXCESSIVE_ACCESS"));
    }
}

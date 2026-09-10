package com.aichainid.credential;

import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.BadRequestException;
import com.aichainid.credential.dto.CredentialCreateRequest;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.credential.dto.CredentialVerificationResponse;
import com.aichainid.credential.entity.Credential;
import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.entity.CredentialType;
import com.aichainid.credential.mapper.CredentialMapper;
import com.aichainid.credential.repository.CredentialRepository;
import com.aichainid.credential.service.CredentialServiceImpl;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.user.entity.User;
import com.aichainid.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private CredentialMapper credentialMapper = new CredentialMapper();

    @InjectMocks
    private CredentialServiceImpl credentialService;

    private User sampleUser;
    private Organization sampleOrg;
    private Credential sampleCredential;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("student@vit.edu").firstName("Om").lastName("Pawar").build();
        sampleOrg = Organization.builder().id(1L).name("VIT Research Institute").build();

        sampleCredential = Credential.builder()
                .id(100L)
                .credentialId("cred-test-12345")
                .user(sampleUser)
                .issuerOrganization(sampleOrg)
                .credentialType(CredentialType.LAB_SAFETY_CERTIFICATE)
                .title("Lab Safety Certification")
                .credentialHash("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3")
                .issuedAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusYears(1))
                .status(CredentialStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should issue credential and anchor hash on blockchain")
    void testIssueCredential() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(sampleOrg));
        when(credentialRepository.save(any(Credential.class))).thenReturn(sampleCredential);

        CredentialCreateRequest request = CredentialCreateRequest.builder()
                .userId(1L)
                .issuerOrganizationId(1L)
                .credentialType(CredentialType.LAB_SAFETY_CERTIFICATE)
                .title("Lab Safety Certification")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .build();

        CredentialResponse response = credentialService.issueCredential(request);

        assertNotNull(response);
        assertEquals(sampleCredential.getCredentialId(), response.getCredentialId());
        assertEquals(CredentialStatus.ACTIVE, response.getStatus());
        verify(blockchainService, times(1)).recordCredentialHash(eq(100L), anyString());
    }

    @Test
    @DisplayName("Should revoke credential and anchor revocation on blockchain")
    void testRevokeCredential() {
        when(credentialRepository.findById(100L)).thenReturn(Optional.of(sampleCredential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(sampleCredential);

        CredentialResponse response = credentialService.revokeCredential(100L, "Safety protocol violation");

        assertEquals(CredentialStatus.REVOKED, response.getStatus());
        verify(blockchainService, times(1)).recordEvent(eq("CREDENTIAL_REVOKED"), eq("CREDENTIAL"), eq(100L), anyString());
    }

    @Test
    @DisplayName("Should fail when trying to revoke an already revoked credential")
    void testRevokeAlreadyRevokedCredential() {
        sampleCredential.setStatus(CredentialStatus.REVOKED);
        when(credentialRepository.findById(100L)).thenReturn(Optional.of(sampleCredential));

        assertThrows(BadRequestException.class, () -> credentialService.revokeCredential(100L, "Already revoked"));
    }
}

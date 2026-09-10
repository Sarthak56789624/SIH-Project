package com.aichainid.identity;

import com.aichainid.audit.service.AuditLogService;
import com.aichainid.common.exception.DuplicateResourceException;
import com.aichainid.identity.dto.IdentityCreateRequest;
import com.aichainid.identity.dto.IdentityResponse;
import com.aichainid.identity.dto.IdentityVerificationResponse;
import com.aichainid.identity.entity.Identity;
import com.aichainid.identity.entity.IdentityStatus;
import com.aichainid.identity.mapper.IdentityMapper;
import com.aichainid.identity.repository.IdentityRepository;
import com.aichainid.identity.service.IdentityServiceImpl;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private IdentityMapper identityMapper = new IdentityMapper();

    @InjectMocks
    private IdentityServiceImpl identityService;

    private User sampleUser;
    private Identity sampleIdentity;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .firstName("Om")
                .lastName("Pawar")
                .email("om@example.com")
                .build();

        sampleIdentity = Identity.builder()
                .id(10L)
                .user(sampleUser)
                .did("did:chainid:test123456789")
                .publicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...")
                .didMethod("did:chainid")
                .status(IdentityStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should successfully create DID and generate public key for user")
    void testCreateIdentitySuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(identityRepository.existsByUserId(1L)).thenReturn(false);
        when(identityRepository.save(any(Identity.class))).thenReturn(sampleIdentity);

        IdentityCreateRequest request = IdentityCreateRequest.builder().userId(1L).build();
        IdentityResponse response = identityService.createIdentity(request);

        assertNotNull(response);
        assertEquals(sampleIdentity.getDid(), response.getDid());
        assertEquals(IdentityStatus.ACTIVE, response.getStatus());
        verify(identityRepository, times(1)).save(any(Identity.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if user already has a DID")
    void testCreateIdentityDuplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(identityRepository.existsByUserId(1L)).thenReturn(true);

        IdentityCreateRequest request = IdentityCreateRequest.builder().userId(1L).build();
        assertThrows(DuplicateResourceException.class, () -> identityService.createIdentity(request));
    }

    @Test
    @DisplayName("Should verify valid active DID successfully")
    void testVerifyIdentityValid() {
        when(identityRepository.findById(10L)).thenReturn(Optional.of(sampleIdentity));

        IdentityVerificationResponse result = identityService.verifyIdentity(10L);

        assertTrue(result.isValid());
        assertEquals(sampleIdentity.getDid(), result.getDid());
        assertEquals(IdentityStatus.ACTIVE, result.getStatus());
    }
}

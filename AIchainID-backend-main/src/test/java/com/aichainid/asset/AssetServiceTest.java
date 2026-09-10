package com.aichainid.asset;

import com.aichainid.asset.dto.AssetAssignmentRequest;
import com.aichainid.asset.dto.AssetAssignmentResponse;
import com.aichainid.asset.dto.AssetCreateRequest;
import com.aichainid.asset.dto.AssetResponse;
import com.aichainid.asset.entity.Asset;
import com.aichainid.asset.entity.AssetAssignment;
import com.aichainid.asset.entity.AssetAssignmentStatus;
import com.aichainid.asset.entity.AssetStatus;
import com.aichainid.asset.entity.AssetType;
import com.aichainid.asset.mapper.AssetMapper;
import com.aichainid.asset.repository.AssetAssignmentRepository;
import com.aichainid.asset.repository.AssetRepository;
import com.aichainid.asset.service.AssetServiceImpl;
import com.aichainid.audit.service.AuditLogService;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.exception.BadRequestException;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.security.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetAssignmentRepository assetAssignmentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private AssetMapper assetMapper = new AssetMapper();

    @InjectMocks
    private AssetServiceImpl assetService;

    private User adminUser;
    private User studentUser;
    private Organization organization;
    private Asset availableAsset;

    @BeforeEach
    void setUp() {
        organization = Organization.builder().id(1L).name("VIT Research Institute").build();

        adminUser = User.builder().id(1L).email("admin@vit.edu").firstName("System").lastName("Admin").organization(organization).build();
        studentUser = User.builder().id(2L).email("student@vit.edu").firstName("Om").lastName("Pawar").organization(organization).build();

        availableAsset = Asset.builder()
                .id(10L)
                .assetCode("LAP-001")
                .name("MacBook Pro")
                .assetType(AssetType.LAPTOP)
                .organization(organization)
                .status(AssetStatus.AVAILABLE)
                .serialNumber("SN-MAC-01")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @Test
    @DisplayName("Should create a new asset successfully")
    void testCreateAsset() {
        when(assetRepository.existsByAssetCode("LAP-001")).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(assetRepository.save(any(Asset.class))).thenReturn(availableAsset);

        AssetCreateRequest req = AssetCreateRequest.builder()
                .assetCode("LAP-001")
                .name("MacBook Pro")
                .assetType(AssetType.LAPTOP)
                .organizationId(1L)
                .serialNumber("SN-MAC-01")
                .build();

        AssetResponse res = assetService.createAsset(req);

        assertNotNull(res);
        assertEquals("LAP-001", res.getAssetCode());
        assertEquals(AssetStatus.AVAILABLE, res.getStatus());
    }

    @Test
    @DisplayName("Should assign available asset and anchor on blockchain")
    void testAssignAssetSuccess() {
        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        AssetAssignment assignment = AssetAssignment.builder()
                .id(100L)
                .asset(availableAsset)
                .user(studentUser)
                .assignedBy(adminUser)
                .assignedAt(LocalDateTime.now())
                .status(AssetAssignmentStatus.ASSIGNED)
                .build();

        when(assetAssignmentRepository.save(any(AssetAssignment.class))).thenReturn(assignment);

        AssetAssignmentRequest req = AssetAssignmentRequest.builder().userId(2L).notes("Assigned for thesis").build();
        AssetAssignmentResponse response = assetService.assignAsset(10L, req);

        assertNotNull(response);
        assertEquals(AssetAssignmentStatus.ASSIGNED, response.getStatus());
        assertEquals(AssetStatus.ASSIGNED, availableAsset.getStatus());
        verify(blockchainService, times(1)).recordAssetAssignment(eq(100L), anyString());
    }

    @Test
    @DisplayName("Should throw BadRequestException if asset is already assigned")
    void testAssignAlreadyAssignedAsset() {
        availableAsset.setStatus(AssetStatus.ASSIGNED);
        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));

        AssetAssignmentRequest req = AssetAssignmentRequest.builder().userId(2L).build();
        assertThrows(BadRequestException.class, () -> assetService.assignAsset(10L, req));
    }
}

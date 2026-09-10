package com.aichainid.config;

import com.aichainid.asset.entity.Asset;
import com.aichainid.asset.entity.AssetStatus;
import com.aichainid.asset.entity.AssetType;
import com.aichainid.asset.repository.AssetRepository;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.identity.entity.Identity;
import com.aichainid.identity.entity.IdentityStatus;
import com.aichainid.identity.repository.IdentityRepository;
import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.entity.OrganizationStatus;
import com.aichainid.organization.entity.OrganizationType;
import com.aichainid.organization.repository.OrganizationRepository;
import com.aichainid.permission.entity.Permission;
import com.aichainid.permission.repository.PermissionRepository;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceStatus;
import com.aichainid.resource.entity.ResourceType;
import com.aichainid.resource.repository.ResourceRepository;
import com.aichainid.role.entity.Role;
import com.aichainid.role.repository.RoleRepository;
import com.aichainid.user.entity.User;
import com.aichainid.user.entity.UserStatus;
import com.aichainid.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final IdentityRepository identityRepository;
    private final ResourceRepository resourceRepository;
    private final AssetRepository assetRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.email:admin@aichainid.org}")
    private String adminEmail;

    @Value("${app.init.admin.password:Admin@12345}")
    private String adminPassword;

    @Value("${app.init.admin.first-name:System}")
    private String adminFirstName;

    @Value("${app.init.admin.last-name:Administrator}")
    private String adminLastName;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and initializing seed data...");

        // 1. Initialize Organization
        Organization org = organizationRepository.findByName("VIT Research Institute")
                .orElseGet(() -> organizationRepository.save(Organization.builder()
                        .name("VIT Research Institute")
                        .description("Premier academic and research university specializing in AI and distributed systems.")
                        .organizationType(OrganizationType.UNIVERSITY)
                        .email("contact@vit.edu")
                        .phone("+91-416-2202020")
                        .address("Vellore, Tamil Nadu, India")
                        .status(OrganizationStatus.ACTIVE)
                        .build()));

        // 2. Initialize Permissions
        String[][] permissionData = {
                {"LAB_VIEW", "View laboratory resources and details", "RESEARCH_LAB", "READ"},
                {"LAB_ACCESS", "Request and access laboratory facilities", "RESEARCH_LAB", "EXECUTE"},
                {"EQUIPMENT_USE", "Operate laboratory equipment and hardware", "LAB_EQUIPMENT", "WRITE"},
                {"DATABASE_READ", "Read data from research databases", "RESEARCH_DATABASE", "READ"},
                {"DATABASE_WRITE", "Write/modify research databases", "RESEARCH_DATABASE", "WRITE"},
                {"ASSET_VIEW", "View registered organization assets", "ASSET", "READ"},
                {"ASSET_ASSIGN", "Assign assets to users and process returns", "ASSET", "WRITE"},
                {"ACCESS_APPROVE", "Approve or reject access requests", "ACCESS_REQUEST", "APPROVE"},
                {"CREDENTIAL_VERIFY", "Verify user credentials and validity", "CREDENTIAL", "READ"}
        };

        Map<String, Permission> permissions = new HashMap<>();
        for (String[] data : permissionData) {
            String name = data[0];
            Permission permission = permissionRepository.findByName(name)
                    .orElseGet(() -> permissionRepository.save(Permission.builder()
                            .name(name)
                            .description(data[1])
                            .resourceType(data[2])
                            .action(data[3])
                            .build()));
            permissions.put(name, permission);
        }

        // 3. Initialize Roles
        createRoleIfNotExists("ADMIN", "System administrator with full platform authority", org,
                new HashSet<>(permissions.values()));

        createRoleIfNotExists("FACULTY", "Faculty members with academic and approval authority", org,
                Set.of(permissions.get("LAB_VIEW"), permissions.get("LAB_ACCESS"), permissions.get("EQUIPMENT_USE"),
                        permissions.get("DATABASE_READ"), permissions.get("DATABASE_WRITE"), permissions.get("ASSET_VIEW"),
                        permissions.get("ACCESS_APPROVE"), permissions.get("CREDENTIAL_VERIFY")));

        createRoleIfNotExists("LAB_MANAGER", "Laboratory facility and asset manager", org,
                Set.of(permissions.get("LAB_VIEW"), permissions.get("LAB_ACCESS"), permissions.get("EQUIPMENT_USE"),
                        permissions.get("ASSET_VIEW"), permissions.get("ASSET_ASSIGN"), permissions.get("ACCESS_APPROVE")));

        createRoleIfNotExists("RESEARCHER", "Postdoc and graduate research scholars", org,
                Set.of(permissions.get("LAB_VIEW"), permissions.get("LAB_ACCESS"), permissions.get("EQUIPMENT_USE"),
                        permissions.get("DATABASE_READ"), permissions.get("ASSET_VIEW")));

        createRoleIfNotExists("STUDENT", "Undergraduate and master students", org,
                Set.of(permissions.get("LAB_VIEW"), permissions.get("LAB_ACCESS"), permissions.get("ASSET_VIEW")));

        // 4. Initialize Admin User
        if (!userRepository.existsByEmail(adminEmail.trim().toLowerCase())) {
            Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
            Set<Role> roles = new HashSet<>();
            if (adminRole != null) {
                roles.add(adminRole);
            }

            User admin = User.builder()
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .email(adminEmail.trim().toLowerCase())
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .phone("+91-9999999999")
                    .status(UserStatus.ACTIVE)
                    .organization(org)
                    .roles(roles)
                    .build();

            User savedAdmin = userRepository.save(admin);

            // Create DID for Admin
            if (!identityRepository.existsByUserId(savedAdmin.getId())) {
                Identity adminIdentity = Identity.builder()
                        .user(savedAdmin)
                        .did(CryptoUtils.generateDid())
                        .publicKey(CryptoUtils.generatePublicKey())
                        .didMethod("did:chainid")
                        .status(IdentityStatus.ACTIVE)
                        .build();
                identityRepository.save(adminIdentity);
            }
            log.info("Initialized default administrator: {}", adminEmail);
        }

        // 5. Initialize Resources
        createResourceIfNotExists("AI Research Lab", "Restricted deep learning and AI research lab",
                ResourceType.RESEARCH_LAB, "Block A - Floor 2", org, ResourceSensitivityLevel.HIGH);

        createResourceIfNotExists("GPU Server", "NVIDIA H100 high-performance multi-GPU compute cluster",
                ResourceType.GPU_SERVER, "Data Center - Rack 4", org, ResourceSensitivityLevel.CRITICAL);

        createResourceIfNotExists("Research Database", "Proprietary research dataset & genomic database",
                ResourceType.RESEARCH_DATABASE, "Cloud Cluster - DB01", org, ResourceSensitivityLevel.HIGH);

        createResourceIfNotExists("Computer Lab", "General purpose computing laboratory",
                ResourceType.COMPUTER_LAB, "Academic Block - Lab 301", org, ResourceSensitivityLevel.LOW);

        createResourceIfNotExists("3D Printer", "Industrial additive manufacturing 3D printer",
                ResourceType.LAB_EQUIPMENT, "Innovation Hub - Workshop", org, ResourceSensitivityLevel.MEDIUM);

        // 6. Initialize Assets
        createAssetIfNotExists("LAP-001", "Research Laptop Dell XPS 15", "Dell XPS 15 64GB RAM",
                AssetType.LAPTOP, org, "SN-998811");

        createAssetIfNotExists("LAP-002", "AI Workstation ThinkPad P1", "Lenovo ThinkPad P1 RTX 4080",
                AssetType.LAPTOP, org, "SN-998822");

        createAssetIfNotExists("ID-001", "Smart NFC Identity Badge", "High-security encrypted smart badge",
                AssetType.ID_CARD, org, "RFID-4411");

        createAssetIfNotExists("GPU-001", "NVIDIA H100 Node Unit", "Dedicated AI accelerator compute box",
                AssetType.GPU_SERVER, org, "GPU-H100-88");

        log.info("Seed data initialization completed successfully.");
    }

    private void createRoleIfNotExists(String name, String description, Organization org, Set<Permission> permissions) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .organization(org)
                    .permissions(permissions)
                    .build();
            roleRepository.save(role);
        }
    }

    private void createResourceIfNotExists(String name, String description, ResourceType type,
                                           String location, Organization org, ResourceSensitivityLevel sensitivity) {
        if (resourceRepository.findByResourceType(type).stream().noneMatch(r -> r.getName().equalsIgnoreCase(name))) {
            Resource resource = Resource.builder()
                    .name(name)
                    .description(description)
                    .resourceType(type)
                    .location(location)
                    .organization(org)
                    .status(ResourceStatus.AVAILABLE)
                    .sensitivityLevel(sensitivity)
                    .build();
            resourceRepository.save(resource);
        }
    }

    private void createAssetIfNotExists(String code, String name, String description,
                                        AssetType type, Organization org, String serial) {
        if (!assetRepository.existsByAssetCode(code)) {
            Asset asset = Asset.builder()
                    .assetCode(code)
                    .name(name)
                    .description(description)
                    .assetType(type)
                    .organization(org)
                    .status(AssetStatus.AVAILABLE)
                    .serialNumber(serial)
                    .build();
            assetRepository.save(asset);
        }
    }
}

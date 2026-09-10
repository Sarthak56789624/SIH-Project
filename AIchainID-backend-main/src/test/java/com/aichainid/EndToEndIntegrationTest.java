package com.aichainid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static String studentToken;
    private static Long studentUserId;
    private static Long studentDidId;
    private static Long credentialId;
    private static Long resourceId;
    private static Long accessRequestId;
    private static Long assetId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Admin logs in and receives JWT token")
    void testAdminLogin() throws Exception {
        Map<String, String> loginReq = Map.of(
                "email", "admin@aichainid.org",
                "password", "Admin@12345"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        adminToken = root.path("data").path("accessToken").asText();
        assertNotNull(adminToken);
    }

    @Test
    @Order(2)
    @DisplayName("Step 2 & 3: Register a new student user")
    void testRegisterStudent() throws Exception {
        Map<String, Object> registerReq = Map.of(
                "firstName", "Om",
                "lastName", "Pawar",
                "email", "ompawar@vit.edu",
                "password", "Password@123",
                "organizationId", 1,
                "phone", "+91-9876543210"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("ompawar@vit.edu"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        studentUserId = root.path("data").path("id").asLong();
        assertTrue(studentUserId > 0);
    }

    @Test
    @Order(3)
    @DisplayName("Step 4 & 5: Student logs in and receives JWT token")
    void testStudentLogin() throws Exception {
        Map<String, String> loginReq = Map.of(
                "email", "ompawar@vit.edu",
                "password", "Password@123"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        studentToken = root.path("data").path("accessToken").asText();
        assertNotNull(studentToken);
    }

    @Test
    @Order(4)
    @DisplayName("Step 6: Create Decentralized Identity (DID) for Student")
    void testCreateStudentDid() throws Exception {
        Map<String, Object> didReq = Map.of(
                "userId", studentUserId
        );

        MvcResult result = mockMvc.perform(post("/api/identities")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(didReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.did").value(org.hamcrest.Matchers.startsWith("did:chainid:")))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        studentDidId = root.path("data").path("id").asLong();
        assertTrue(studentDidId > 0);
    }

    @Test
    @Order(5)
    @DisplayName("Step 7 & 8: Issue Verifiable Credential to Student and verify it")
    void testIssueAndVerifyCredential() throws Exception {
        Map<String, Object> credReq = Map.of(
                "userId", studentUserId,
                "issuerOrganizationId", 1,
                "credentialType", "LAB_SAFETY_CERTIFICATE",
                "title", "Advanced Lab Safety Certificate",
                "expiryDate", LocalDateTime.now().plusMonths(6).toString()
        );

        MvcResult issueResult = mockMvc.perform(post("/api/credentials")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.credentialHash").exists())
                .andReturn();

        JsonNode credRoot = objectMapper.readTree(issueResult.getResponse().getContentAsString());
        credentialId = credRoot.path("data").path("id").asLong();

        // Verify Credential
        mockMvc.perform(post("/api/credentials/" + credentialId + "/verify")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.hashIntact").value(true));
    }

    @Test
    @Order(6)
    @DisplayName("Step 9 & 10: Create Resource and Student requests Access")
    void testCreateResourceAndSubmitAccessRequest() throws Exception {
        Map<String, Object> resReq = Map.of(
                "name", "Quantum AI Laboratory",
                "description", "Specialized Quantum AI laboratory",
                "resourceType", "RESEARCH_LAB",
                "location", "Science Block - 4th Floor",
                "organizationId", 1,
                "sensitivityLevel", "MEDIUM"
        );

        MvcResult resResult = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resReq)))
                .andExpect(status().isCreated())
                .andReturn();

        resourceId = objectMapper.readTree(resResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Student requests access
        Map<String, Object> accessReq = Map.of(
                "resourceId", resourceId,
                "reason", "Quantum computing experiment for final year thesis",
                "requestedFrom", LocalDateTime.now().plusHours(1).toString(),
                "requestedUntil", LocalDateTime.now().plusHours(4).toString()
        );

        MvcResult accessResult = mockMvc.perform(post("/api/access-requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accessReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskAssessment").exists())
                .andReturn();

        accessRequestId = objectMapper.readTree(accessResult.getResponse().getContentAsString()).path("data").path("id").asLong();
        assertTrue(accessRequestId > 0);
    }

    @Test
    @Order(7)
    @DisplayName("Step 14 to 20: Evaluate AI Risk, Admin approves request, anchors on Blockchain")
    void testRiskEvaluationAndAdminApproval() throws Exception {
        // AI Risk evaluation
        mockMvc.perform(post("/api/access-requests/" + accessRequestId + "/evaluate-risk")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.riskScore").exists())
                .andExpect(jsonPath("$.data.recommendation").exists());

        // Admin approves request
        Map<String, String> approveReq = Map.of("reason", "Approved for verified research thesis");
        mockMvc.perform(post("/api/access-requests/" + accessRequestId + "/approve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.decision").value("APPROVED"));

        // Verify request is now APPROVED
        mockMvc.perform(get("/api/access-requests/" + accessRequestId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @Order(8)
    @DisplayName("Step 21 to 24: Asset assignment, return, and audit log history")
    void testAssetAssignmentLifecycle() throws Exception {
        Map<String, Object> assetReq = Map.of(
                "assetCode", "LAP-TEST-009",
                "name", "Deep Learning Laptop",
                "description", "Laptop for AI training",
                "assetType", "LAPTOP",
                "organizationId", 1,
                "serialNumber", "SN-DL-009"
        );

        MvcResult assetResult = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetReq)))
                .andExpect(status().isCreated())
                .andReturn();

        assetId = objectMapper.readTree(assetResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Assign asset to student
        Map<String, Object> assignReq = Map.of(
                "userId", studentUserId,
                "notes", "Assigned for research project"
        );

        mockMvc.perform(post("/api/assets/" + assetId + "/assign")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        // Process Asset Return
        mockMvc.perform(post("/api/assets/" + assetId + "/return")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("notes", "Returned in perfect condition"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RETURNED"));

        // Verify Blockchain Records
        mockMvc.perform(get("/api/blockchain/records")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(9)
    @DisplayName("Step 25 & 26: Admin revokes access permission and creates revocation blockchain record")
    void testRevokeAccessRequest() throws Exception {
        Map<String, String> revokeReq = Map.of("reason", "Research activity completed ahead of schedule");

        mockMvc.perform(post("/api/access-requests/" + accessRequestId + "/revoke")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revokeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REVOKED"));
    }
}

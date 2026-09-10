package com.aichainid.access.controller;

import com.aichainid.access.dto.AccessRequestCreateRequest;
import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.access.service.AccessRequestService;
import com.aichainid.approval.dto.ApprovalDecisionRequest;
import com.aichainid.approval.dto.ApprovalResponse;
import com.aichainid.common.response.ApiResponse;
import com.aichainid.risk.dto.RiskAssessmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Access Requests", description = "Decentralized access request workflow, AI risk evaluation, and admin approvals")
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    @PostMapping("/api/access-requests")
    @Operation(summary = "Submit a new access request (authenticated user)")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> createAccessRequest(
            @Valid @RequestBody AccessRequestCreateRequest request) {
        AccessRequestResponse response = accessRequestService.createAccessRequest(request);
        return new ResponseEntity<>(ApiResponse.created("Access request submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/api/access-requests")
    @Operation(summary = "Get all access requests")
    public ResponseEntity<ApiResponse<List<AccessRequestResponse>>> getAllAccessRequests() {
        List<AccessRequestResponse> requests = accessRequestService.getAllAccessRequests();
        return ResponseEntity.ok(ApiResponse.success("Access requests retrieved successfully", requests));
    }

    @GetMapping("/api/access-requests/{id}")
    @Operation(summary = "Get access request details with risk assessment and approval history")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> getAccessRequestById(@PathVariable Long id) {
        AccessRequestResponse request = accessRequestService.getAccessRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Access request retrieved successfully", request));
    }

    @GetMapping("/api/users/{userId}/access-requests")
    @Operation(summary = "Get all access requests submitted by a specific user")
    public ResponseEntity<ApiResponse<List<AccessRequestResponse>>> getAccessRequestsByUserId(@PathVariable Long userId) {
        List<AccessRequestResponse> requests = accessRequestService.getAccessRequestsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User access requests retrieved successfully", requests));
    }

    @PostMapping("/api/access-requests/{id}/cancel")
    @Operation(summary = "Cancel a pending access request")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> cancelAccessRequest(@PathVariable Long id) {
        AccessRequestResponse cancelled = accessRequestService.cancelAccessRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Access request cancelled successfully", cancelled));
    }

    @PostMapping("/api/access-requests/{id}/evaluate-risk")
    @Operation(summary = "Trigger AI Risk Engine analysis on an access request")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> evaluateRisk(@PathVariable Long id) {
        RiskAssessmentResponse risk = accessRequestService.evaluateRisk(id);
        return ResponseEntity.ok(ApiResponse.success("AI risk evaluation completed", risk));
    }

    @PostMapping("/api/access-requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'FACULTY')")
    @Operation(summary = "Approve access request (Admin/Approver)")
    public ResponseEntity<ApiResponse<ApprovalResponse>> approveAccessRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest request) {
        ApprovalResponse approval = accessRequestService.approveAccessRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Access request approved successfully", approval));
    }

    @PostMapping("/api/access-requests/{id}/break-glass")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
    @Operation(summary = "Invoke Break-Glass Emergency Override (Admin/Lab Manager)")
    public ResponseEntity<ApiResponse<ApprovalResponse>> breakGlassAccessRequest(
            @PathVariable Long id,
            @RequestBody ApprovalDecisionRequest request) {
        ApprovalResponse approval = accessRequestService.breakGlassAccessRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("🚨 Break-Glass emergency override granted and anchored to blockchain", approval));
    }

    @PostMapping("/api/access-requests/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'FACULTY')")
    @Operation(summary = "Reject access request (Admin/Approver)")
    public ResponseEntity<ApiResponse<ApprovalResponse>> rejectAccessRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest request) {
        ApprovalResponse rejection = accessRequestService.rejectAccessRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Access request rejected successfully", rejection));
    }

    @PostMapping("/api/access-requests/{id}/revoke")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'FACULTY')")
    @Operation(summary = "Revoke an approved active access permission")
    public ResponseEntity<ApiResponse<AccessRequestResponse>> revokeAccessRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest request) {
        AccessRequestResponse revoked = accessRequestService.revokeAccessRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Access permission revoked successfully", revoked));
    }
}

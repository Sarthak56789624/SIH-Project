package com.aichainid.blockchain.controller;

import com.aichainid.blockchain.dto.BlockchainRecordResponse;
import com.aichainid.blockchain.service.BlockchainService;
import com.aichainid.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
@Tag(name = "Blockchain Ledger", description = "On-chain transaction records and cryptographic integrity verification")
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/records")
    @Operation(summary = "Get all immutable blockchain records")
    public ResponseEntity<ApiResponse<List<BlockchainRecordResponse>>> getAllRecords() {
        List<BlockchainRecordResponse> records = blockchainService.getAllRecords();
        return ResponseEntity.ok(ApiResponse.success("Blockchain records retrieved successfully", records));
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "Get blockchain record by ID")
    public ResponseEntity<ApiResponse<BlockchainRecordResponse>> getRecordById(@PathVariable Long id) {
        BlockchainRecordResponse record = blockchainService.getRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Blockchain record retrieved successfully", record));
    }

    @GetMapping("/records/tx/{txHash}")
    @Operation(summary = "Lookup on-chain record by transaction hash")
    public ResponseEntity<ApiResponse<BlockchainRecordResponse>> getRecordByTxHash(@PathVariable String txHash) {
        BlockchainRecordResponse record = blockchainService.getRecordByTxHash(txHash);
        return ResponseEntity.ok(ApiResponse.success("Blockchain record retrieved successfully", record));
    }

    @PostMapping("/verify-hash")
    @Operation(summary = "Cryptographically verify hash integrity against blockchain record")
    public ResponseEntity<ApiResponse<Boolean>> verifyHash(@RequestBody Map<String, String> request) {
        String txHash = request.get("txHash");
        String expectedHash = request.get("expectedHash");
        boolean valid = blockchainService.verifyRecordIntegrity(txHash, expectedHash);
        return ResponseEntity.ok(ApiResponse.success("Hash verification completed", valid));
    }

    @GetMapping("/verify-ledger")
    @Operation(summary = "Cryptographically audit entire blockchain ledger block-hash links (Tamper Audit)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyLedger() {
        boolean intact = blockchainService.verifyLedgerIntegrity();
        Map<String, Object> response = Map.of(
                "ledgerIntact", intact,
                "status", intact ? "CRYPTOGRAPHIC_CONSENSUS_VALID" : "TAMPER_DETECTED_HASH_BROKEN",
                "message", intact ? "All block-headers and hash chains are mathematically verified." 
                                  : "CRITICAL ALERT: Hash chain broken! An unauthorized database alteration has occurred."
        );
        return ResponseEntity.ok(ApiResponse.success("Ledger integrity audit complete", response));
    }

    @PostMapping("/verify-edge-access")
    @Operation(summary = "Edge Gate (Lab Door / GPU Server) Zero-Trust Access Verification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEdgeAccess(@RequestBody Map<String, Long> request) {
        Long resourceId = request.get("resourceId");
        Long userId = request.get("userId");
        Map<String, Object> result = blockchainService.verifyEdgeAccess(resourceId, userId);
        return ResponseEntity.ok(ApiResponse.success("Edge gate verification completed", result));
    }
}

package com.aichainid.blockchain.service;

import com.aichainid.blockchain.dto.BlockchainRecordResponse;
import com.aichainid.blockchain.entity.BlockchainNetwork;
import com.aichainid.blockchain.entity.BlockchainRecord;
import com.aichainid.blockchain.entity.BlockchainStatus;
import com.aichainid.blockchain.mapper.BlockchainRecordMapper;
import com.aichainid.blockchain.repository.BlockchainRecordRepository;
import com.aichainid.common.exception.ResourceNotFoundException;
import com.aichainid.common.util.CryptoUtils;
import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import com.aichainid.access.repository.AccessRequestRepository;
import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockBlockchainService implements BlockchainService {

    private final BlockchainRecordRepository blockchainRecordRepository;
    private final BlockchainRecordMapper blockchainRecordMapper;
    private final AccessRequestRepository accessRequestRepository;
    private final ResourceRepository resourceRepository;
    private final AtomicLong blockCounter = new AtomicLong(1000000L);

    @Override
    @Transactional
    public BlockchainRecord recordEvent(String eventType, String entityType, Long entityId, String recordHash) {
        String finalRecordHash = recordHash != null ? recordHash : CryptoUtils.sha256Hex(eventType + ":" + entityId);
        
        // Cryptographic Block Chaining: get latest block hash to link
        String previousHash = blockchainRecordRepository.findTopByOrderByBlockNumberDesc()
                .map(BlockchainRecord::getTransactionHash)
                .orElse("0x0000000000000000000000000000000000000000000000000000000000000000"); // Genesis parent

        long blockNumber = blockCounter.incrementAndGet();
        // Deterministic block transaction hash chained with previousHash
        String txHash = "0x" + CryptoUtils.sha256Hex(previousHash + ":" + finalRecordHash + ":" + blockNumber);

        BlockchainRecord record = BlockchainRecord.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .transactionHash(txHash)
                .blockchainNetwork(BlockchainNetwork.LOCAL_SIMULATION)
                .blockNumber(blockNumber)
                .recordHash(finalRecordHash)
                .previousHash(previousHash)
                .isBreakGlass(false)
                .policySignature("SIG_ZERO_TRUST_" + Long.toHexString(blockNumber))
                .status(BlockchainStatus.CONFIRMED)
                .build();

        BlockchainRecord saved = blockchainRecordRepository.save(record);
        log.info("BLOCKCHAIN_LEDGER: Anchored event [{}] on Block #{}. TxHash: {}, PrevHash: {}",
                eventType, blockNumber, txHash, previousHash);
        return saved;
    }

    @Override
    @Transactional
    public BlockchainRecord recordAccessApproval(Long accessRequestId, String recordHash) {
        return recordEvent("ACCESS_APPROVED", "ACCESS_REQUEST", accessRequestId, recordHash);
    }

    @Override
    @Transactional
    public BlockchainRecord recordAccessRejection(Long accessRequestId, String recordHash) {
        return recordEvent("ACCESS_REJECTED", "ACCESS_REQUEST", accessRequestId, recordHash);
    }

    @Override
    @Transactional
    public BlockchainRecord recordAccessRevocation(Long accessRequestId, String recordHash) {
        return recordEvent("ACCESS_REVOKED", "ACCESS_REQUEST", accessRequestId, recordHash);
    }

    @Override
    @Transactional
    public BlockchainRecord recordCredentialHash(Long credentialId, String credentialHash) {
        return recordEvent("CREDENTIAL_HASH", "CREDENTIAL", credentialId, credentialHash);
    }

    @Override
    @Transactional
    public BlockchainRecord recordAssetAssignment(Long assetAssignmentId, String recordHash) {
        return recordEvent("ASSET_ASSIGNED", "ASSET_ASSIGNMENT", assetAssignmentId, recordHash);
    }

    @Override
    @Transactional
    public BlockchainRecord recordAssetReturn(Long assetAssignmentId, String recordHash) {
        return recordEvent("ASSET_RETURNED", "ASSET_ASSIGNMENT", assetAssignmentId, recordHash);
    }

    @Override
    @Transactional
    public BlockchainRecord recordPermissionChange(Long roleId, String recordHash) {
        return recordEvent("PERMISSION_CHANGED", "ROLE", roleId, recordHash);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockchainRecordResponse> getAllRecords() {
        return blockchainRecordRepository.findAll().stream()
                .map(blockchainRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BlockchainRecordResponse getRecordById(Long id) {
        BlockchainRecord record = blockchainRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlockchainRecord", "id", id));
        return blockchainRecordMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockchainRecordResponse getRecordByTxHash(String txHash) {
        BlockchainRecord record = blockchainRecordRepository.findByTransactionHash(txHash)
                .orElseThrow(() -> new ResourceNotFoundException("BlockchainRecord", "transactionHash", txHash));
        return blockchainRecordMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockchainRecordResponse> getRecordsByEntity(String entityType, Long entityId) {
        return blockchainRecordRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(blockchainRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BlockchainRecord recordBreakGlassApproval(Long accessRequestId, String reason, Long adminId, String recordHash) {
        String finalRecordHash = recordHash != null ? recordHash : CryptoUtils.sha256Hex("BREAK_GLASS:" + accessRequestId + ":" + adminId + ":" + reason);

        String previousHash = blockchainRecordRepository.findTopByOrderByBlockNumberDesc()
                .map(BlockchainRecord::getTransactionHash)
                .orElse("0x0000000000000000000000000000000000000000000000000000000000000000");

        long blockNumber = blockCounter.incrementAndGet();
        String txHash = "0x" + CryptoUtils.sha256Hex(previousHash + ":" + finalRecordHash + ":" + blockNumber);

        BlockchainRecord record = BlockchainRecord.builder()
                .eventType("BREAK_GLASS_TRIGGERED")
                .entityType("ACCESS_REQUEST")
                .entityId(accessRequestId)
                .transactionHash(txHash)
                .blockchainNetwork(BlockchainNetwork.LOCAL_SIMULATION)
                .blockNumber(blockNumber)
                .recordHash(finalRecordHash)
                .previousHash(previousHash)
                .isBreakGlass(true)
                .policySignature("EMERGENCY_OVERRIDE_ADMIN_" + adminId + "_BLOCK_" + blockNumber)
                .status(BlockchainStatus.CONFIRMED)
                .build();

        BlockchainRecord saved = blockchainRecordRepository.save(record);
        log.warn("🚨 BLOCKCHAIN_LEDGER: [BREAK-GLASS EMERGENCY] Invoked on Block #{}. Request: {}, Admin: {}, TxHash: {}",
                blockNumber, accessRequestId, adminId, txHash);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyRecordIntegrity(String txHash, String expectedHash) {
        return blockchainRecordRepository.findByTransactionHash(txHash)
                .map(record -> record.getRecordHash().equalsIgnoreCase(expectedHash))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyLedgerIntegrity() {
        List<BlockchainRecord> records = blockchainRecordRepository.findAll();
        if (records.isEmpty()) {
            return true;
        }

        String expectedPreviousHash = "0x0000000000000000000000000000000000000000000000000000000000000000";
        for (int i = 0; i < records.size(); i++) {
            BlockchainRecord current = records.get(i);
            if (i > 0) {
                // Verify link to previous block
                if (current.getPreviousHash() != null && !current.getPreviousHash().equalsIgnoreCase(expectedPreviousHash)) {
                    log.error("LEDGER INTEGRITY BREACH: Block #{} previousHash mismatch! Expected: {}, Found: {}",
                            current.getBlockNumber(), expectedPreviousHash, current.getPreviousHash());
                    return false;
                }
            }
            expectedPreviousHash = current.getTransactionHash();
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> verifyEdgeAccess(Long resourceId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) {
            result.put("accessGranted", false);
            result.put("reason", "Resource does not exist");
            return result;
        }

        // 1. Check database for an APPROVED access request
        List<AccessRequest> dbRequests = accessRequestRepository.findByRequesterIdAndResourceIdAndStatus(
                userId, resourceId, AccessRequestStatus.APPROVED);

        if (dbRequests.isEmpty()) {
            result.put("accessGranted", false);
            result.put("reason", "No approved access request found in local database");
            return result;
        }

        AccessRequest activeRequest = dbRequests.get(0);

        // 2. Cryptographically verify against the Blockchain Ledger (Zero-Trust Gate)
        List<BlockchainRecord> chainRecords = blockchainRecordRepository.findByEntityTypeAndEntityId(
                "ACCESS_REQUEST", activeRequest.getId());

        boolean hasApprovedBlock = chainRecords.stream().anyMatch(r ->
                "ACCESS_APPROVED".equals(r.getEventType()) || "BREAK_GLASS_TRIGGERED".equals(r.getEventType()));

        boolean hasRevokedBlock = chainRecords.stream().anyMatch(r ->
                "ACCESS_REVOKED".equals(r.getEventType()));

        if (!hasApprovedBlock) {
            // DATABASE TAMPERING DETECTED! (e.g. Someone ran manual SQL UPDATE to grant access)
            log.error("ZERO-TRUST VIOLATION: Database has APPROVED status for requestId {} but NO matching blockchain record exists!",
                    activeRequest.getId());
            result.put("accessGranted", false);
            result.put("tamperDetected", true);
            result.put("reason", "ZERO-TRUST ALERT: Local database claims APPROVED, but missing cryptographic blockchain consensus record! Access DENIED.");
            return result;
        }

        if (hasRevokedBlock) {
            result.put("accessGranted", false);
            result.put("reason", "Cryptographic consensus indicates access has been REVOKED on-chain.");
            return result;
        }

        // Determine if this is normal or break-glass access
        boolean isBreakGlass = chainRecords.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsBreakGlass()));

        result.put("accessGranted", true);
        result.put("isBreakGlass", isBreakGlass);
        result.put("accessRequestId", activeRequest.getId());
        result.put("resourceName", resource.getName());
        result.put("verificationMode", "BLOCKCHAIN_CONSENSUS_VERIFIED");
        result.put("reason", isBreakGlass
                ? "ACCESS GRANTED [EMERGENCY BREAK-GLASS OVERRIDE]: Verified against immutable blockchain ledger."
                : "ACCESS GRANTED: Verified against zero-trust blockchain ledger.");

        return result;
    }
}

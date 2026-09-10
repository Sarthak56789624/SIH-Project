package com.aichainid.blockchain.service;

import com.aichainid.blockchain.dto.BlockchainRecordResponse;
import com.aichainid.blockchain.entity.BlockchainRecord;

import java.util.List;
import java.util.Map;

public interface BlockchainService {

    BlockchainRecord recordEvent(String eventType, String entityType, Long entityId, String recordHash);

    BlockchainRecord recordAccessApproval(Long accessRequestId, String recordHash);

    BlockchainRecord recordAccessRejection(Long accessRequestId, String recordHash);

    BlockchainRecord recordAccessRevocation(Long accessRequestId, String recordHash);

    BlockchainRecord recordCredentialHash(Long credentialId, String credentialHash);

    BlockchainRecord recordAssetAssignment(Long assetAssignmentId, String recordHash);

    BlockchainRecord recordAssetReturn(Long assetAssignmentId, String recordHash);

    BlockchainRecord recordPermissionChange(Long roleId, String recordHash);
 
    BlockchainRecord recordBreakGlassApproval(Long accessRequestId, String reason, Long adminId, String recordHash);

    List<BlockchainRecordResponse> getAllRecords();

    BlockchainRecordResponse getRecordById(Long id);

    BlockchainRecordResponse getRecordByTxHash(String txHash);

    List<BlockchainRecordResponse> getRecordsByEntity(String entityType, Long entityId);

    boolean verifyRecordIntegrity(String txHash, String expectedHash);

    boolean verifyLedgerIntegrity();

    Map<String, Object> verifyEdgeAccess(Long resourceId, Long userId);
}

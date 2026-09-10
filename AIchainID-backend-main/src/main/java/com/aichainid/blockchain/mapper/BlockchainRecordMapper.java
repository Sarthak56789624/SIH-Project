package com.aichainid.blockchain.mapper;

import com.aichainid.blockchain.dto.BlockchainRecordResponse;
import com.aichainid.blockchain.entity.BlockchainRecord;
import org.springframework.stereotype.Component;

@Component
public class BlockchainRecordMapper {

    public BlockchainRecordResponse toResponse(BlockchainRecord record) {
        if (record == null) {
            return null;
        }
        return BlockchainRecordResponse.builder()
                .id(record.getId())
                .eventType(record.getEventType())
                .entityType(record.getEntityType())
                .entityId(record.getEntityId())
                .transactionHash(record.getTransactionHash())
                .blockchainNetwork(record.getBlockchainNetwork())
                .blockNumber(record.getBlockNumber())
                .recordHash(record.getRecordHash())
                .status(record.getStatus())
                .previousHash(record.getPreviousHash())
                .isBreakGlass(record.getIsBreakGlass())
                .policySignature(record.getPolicySignature())
                .createdAt(record.getCreatedAt())
                .build();
    }
}

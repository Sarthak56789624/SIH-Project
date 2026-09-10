package com.aichainid.blockchain.dto;

import com.aichainid.blockchain.entity.BlockchainNetwork;
import com.aichainid.blockchain.entity.BlockchainStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainRecordResponse {

    private Long id;
    private String eventType;
    private String entityType;
    private Long entityId;
    private String transactionHash;
    private BlockchainNetwork blockchainNetwork;
    private Long blockNumber;
    private String recordHash;
    private BlockchainStatus status;
    private String previousHash;
    private Boolean isBreakGlass;
    private String policySignature;
    private LocalDateTime createdAt;
}

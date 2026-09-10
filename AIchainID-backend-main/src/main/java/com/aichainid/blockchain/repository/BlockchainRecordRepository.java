package com.aichainid.blockchain.repository;

import com.aichainid.blockchain.entity.BlockchainRecord;
import com.aichainid.blockchain.entity.BlockchainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, Long> {
    Optional<BlockchainRecord> findByTransactionHash(String transactionHash);
    List<BlockchainRecord> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<BlockchainRecord> findByEventType(String eventType);
    List<BlockchainRecord> findByStatus(BlockchainStatus status);
    Optional<BlockchainRecord> findTopByOrderByBlockNumberDesc();
}

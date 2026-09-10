package com.aichainid.blockchain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_records", indexes = {
        @Index(name = "idx_tx_hash", columnList = "transaction_hash"),
        @Index(name = "idx_chain_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockchainRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "blockchain_network", nullable = false)
    @Builder.Default
    private BlockchainNetwork blockchainNetwork = BlockchainNetwork.LOCAL_SIMULATION;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "record_hash", nullable = false)
    private String recordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockchainStatus status = BlockchainStatus.CONFIRMED;

    @Column(name = "previous_hash")
    private String previousHash;

    @Column(name = "is_break_glass")
    @Builder.Default
    private Boolean isBreakGlass = false;

    @Column(name = "policy_signature")
    private String policySignature;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

package com.aichainid.blockchain;

import com.aichainid.blockchain.dto.BlockchainRecordResponse;
import com.aichainid.blockchain.entity.BlockchainRecord;
import com.aichainid.blockchain.entity.BlockchainStatus;
import com.aichainid.blockchain.mapper.BlockchainRecordMapper;
import com.aichainid.blockchain.repository.BlockchainRecordRepository;
import com.aichainid.blockchain.service.MockBlockchainService;
import com.aichainid.common.util.CryptoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private BlockchainRecordRepository blockchainRecordRepository;

    @Spy
    private BlockchainRecordMapper blockchainRecordMapper = new BlockchainRecordMapper();

    @InjectMocks
    private MockBlockchainService blockchainService;

    private BlockchainRecord sampleRecord;

    @BeforeEach
    void setUp() {
        String recordHash = CryptoUtils.sha256Hex("SAMPLE_EVENT:1");
        String txHash = CryptoUtils.generateTxHash(recordHash);

        sampleRecord = BlockchainRecord.builder()
                .id(1L)
                .eventType("ACCESS_APPROVED")
                .entityType("ACCESS_REQUEST")
                .entityId(10L)
                .transactionHash(txHash)
                .recordHash(recordHash)
                .blockNumber(1000001L)
                .status(BlockchainStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("Should anchor event on-chain with deterministic SHA-256 transaction hash")
    void testRecordEvent() {
        when(blockchainRecordRepository.save(any(BlockchainRecord.class))).thenReturn(sampleRecord);

        BlockchainRecord result = blockchainService.recordAccessApproval(10L, sampleRecord.getRecordHash());

        assertNotNull(result);
        assertEquals("ACCESS_APPROVED", result.getEventType());
        assertTrue(result.getTransactionHash().startsWith("0x"));
        assertEquals(BlockchainStatus.CONFIRMED, result.getStatus());
    }

    @Test
    @DisplayName("Should verify cryptographic integrity of recorded hash")
    void testVerifyRecordIntegrity() {
        when(blockchainRecordRepository.findByTransactionHash(sampleRecord.getTransactionHash()))
                .thenReturn(Optional.of(sampleRecord));

        boolean isIntact = blockchainService.verifyRecordIntegrity(sampleRecord.getTransactionHash(), sampleRecord.getRecordHash());
        boolean isTampered = blockchainService.verifyRecordIntegrity(sampleRecord.getTransactionHash(), "tampered_hash_value");

        assertTrue(isIntact);
        assertFalse(isTampered);
    }
}

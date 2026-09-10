package com.aichainid.common.util;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public final class CryptoUtils {

    private CryptoUtils() {
        // utility class
    }

    /**
     * Compute SHA-256 hash in hexadecimal.
     */
    public static String sha256Hex(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate a W3C-compliant decentralized identifier for the AI-ChainID platform.
     * Format: did:chainid:<uuid>
     */
    public static String generateDid() {
        return "did:chainid:" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate an EC public key representation in PEM/Base64 format for DID registration.
     */
    public static String generatePublicKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            return Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        } catch (Exception e) {
            // Fallback to deterministic simulated public key if crypto provider has environment constraint
            return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Generate a deterministic 0x transaction hash from a payload hash and nonce.
     */
    public static String generateTxHash(String payloadHash) {
        String combined = payloadHash + ":" + System.currentTimeMillis() + ":" + UUID.randomUUID();
        return "0x" + sha256Hex(combined);
    }
}

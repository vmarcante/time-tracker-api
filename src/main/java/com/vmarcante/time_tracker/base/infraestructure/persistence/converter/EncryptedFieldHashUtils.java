package com.vmarcante.time_tracker.base.infraestructure.persistence.converter;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EncryptedFieldHashUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private static String encryptionKey;

    @Value("${app.encryption.key:time-tracker-digital-default-key-change-in-production}")
    public void setEncryptionKey(String key) {
        encryptionKey = key;
    }

    /**
     * Generates a deterministic HMAC-SHA256 hash of the given value.
     * Normalizes the value to lowercase before hashing to allow case-insensitive search.
     * Returns null if value is null or blank.
     */
    public static String generateSearchHash(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            String normalized = value.strip().toLowerCase();
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("[EncryptedFieldHash] Error generating search hash", e);
            throw new RuntimeException("Error generating search hash", e);
        }
    }

}

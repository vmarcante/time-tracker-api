package com.vmarcante.time_tracker.base.infraestructure.persistence.converter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter
@Component
@Slf4j
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private static String encryptionKey;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.encryption.key:time-tracker-digital-default-key-change-in-production}")
    public void setEncryptionKey(String key) {
        encryptionKey = key;
    }

    /**
     * Derives an AES-256 key from the configured key using SHA-256
     */
    private SecretKey getKey() throws Exception {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new IllegalStateException("Encryption key not configured");
        }

        // Use SHA-256 to derive a 256-bit key
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generates a random 12-byte (96-bit) IV for GCM
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }

        try {
            // Generate random IV for this record
            byte[] iv = generateIV();

            // Configure cipher with GCM
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), parameterSpec);

            // Encrypt the data
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            // Encode to Base64 for storage
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("[Encryption] Error encrypting data", e);
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }

        try {
            // Decode from Base64
            byte[] decodedData = Base64.getDecoder().decode(dbData);

            // Extract IV (first 12 bytes) and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            // Configure cipher with extracted IV
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), parameterSpec);

            // Decrypt the data
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("[Encryption] Error decrypting data", e);
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}

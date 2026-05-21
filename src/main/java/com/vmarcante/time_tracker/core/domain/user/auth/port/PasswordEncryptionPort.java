package com.vmarcante.time_tracker.core.domain.user.auth.port;

public interface PasswordEncryptionPort {

    String encrypt(String rawPassword);

    boolean matches(String rawPassword, String encryptedPassword);
}

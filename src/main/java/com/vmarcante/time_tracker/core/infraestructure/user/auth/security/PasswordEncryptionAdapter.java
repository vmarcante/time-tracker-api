package com.vmarcante.time_tracker.core.infraestructure.user.auth.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.auth.port.PasswordEncryptionPort;

@Component
public class PasswordEncryptionAdapter implements PasswordEncryptionPort {

    private final Argon2PasswordEncoder encoder;

    public PasswordEncryptionAdapter() {
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Override
    public String encrypt(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encryptedPassword) {
        return encoder.matches(rawPassword, encryptedPassword);
    }
}

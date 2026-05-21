package com.vmarcante.time_tracker.core.domain.user.auth.services;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.exception.DomainException;
import com.vmarcante.time_tracker.core.domain.user.auth.port.PasswordEncryptionPort;

@Service
public class GeneratePasswordHashService {

    private final PasswordEncryptionPort passwordEncryption;

    public GeneratePasswordHashService(PasswordEncryptionPort passwordEncryption) {
        this.passwordEncryption = passwordEncryption;
    }

    public String execute(String password, String salt) throws DomainException {
        if (password == null || password.isEmpty()) {
            throw new DomainException("domain.password.required", null);
        }
        if (salt == null || salt.isEmpty()) {
            throw new DomainException("domain.salt.required", null);
        }

        String passwordWithSalt = password + salt;
        return passwordEncryption.encrypt(passwordWithSalt);
    }
}

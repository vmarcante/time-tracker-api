package com.vmarcante.time_tracker.core.domain.user.auth.services;

import java.util.Base64;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.shared.utils.SecurityUtils;

@Service
public class GeneratePasswordSaltService {

    private static final int SALT_SIZE_BYTES = 16;

    public String execute() {
        byte[] saltBytes = new byte[SALT_SIZE_BYTES];
        SecurityUtils.createSecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}

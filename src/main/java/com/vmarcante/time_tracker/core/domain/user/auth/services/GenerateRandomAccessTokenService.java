package com.vmarcante.time_tracker.core.domain.user.auth.services;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.shared.utils.SecurityUtils;

@Service
public class GenerateRandomAccessTokenService {

    private static final int TOKEN_LENGTH = 32;
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String execute() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = SecurityUtils.createSecureRandom().nextInt(ALPHANUMERIC_CHARS.length());
            token.append(ALPHANUMERIC_CHARS.charAt(index));
        }
        
        return token.toString();
    }
}

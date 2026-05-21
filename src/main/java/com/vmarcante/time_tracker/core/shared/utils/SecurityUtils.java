package com.vmarcante.time_tracker.core.shared.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecureRandom createSecureRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            return new SecureRandom();
        }
    }
}

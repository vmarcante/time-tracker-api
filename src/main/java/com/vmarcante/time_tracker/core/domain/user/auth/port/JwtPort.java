package com.vmarcante.time_tracker.core.domain.user.auth.port;

import java.util.Date;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;

public interface JwtPort {

    String generateAccessToken(UserAuth userAuth);

    String generateRefreshToken(UserAuth userAuth);

    boolean validateToken(String token);

    UUID extractUserId(String token);

    String extractUsername(String token);

    Integer extractSeqId(String token);

    Long getExpirationTime(String token);

    String extractTokenType(String token);

    String extractIssuer(String token);

    String extractAudience(String token);

    String extractJti(String token);

    Date extractNotBefore(String token);

    String extractLocale(String token);
}
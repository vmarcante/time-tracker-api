package com.vmarcante.time_tracker.core.domain.user.session.port;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

public interface RefreshTokenCachePort {

    void cacheToken(String refreshToken, UUID sessionId, Duration ttl);

    UUID getSessionId(String refreshToken);

    void invalidateToken(String refreshToken);

    void invalidateAllByUserId(UUID userId, Set<String> refreshTokens);
}

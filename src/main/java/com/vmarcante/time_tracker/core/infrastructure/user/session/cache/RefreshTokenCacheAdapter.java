package com.vmarcante.time_tracker.core.infrastructure.user.session.cache;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.session.port.RefreshTokenCachePort;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RefreshTokenCacheAdapter implements RefreshTokenCachePort {

    private static final String CACHE_NAME = "refreshTokens";
    private final CacheManager cacheManager;

    public RefreshTokenCacheAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void cacheToken(String refreshToken, UUID sessionId, Duration ttl) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {

                cache.put(refreshToken, sessionId.toString());
                log.debug("[RefreshTokenCache] Token cached: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.warn("[RefreshTokenCache] Failed to cache token: {}", e.getMessage());
        }
    }

    @Override
    public UUID getSessionId(String refreshToken) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(refreshToken);
                if (wrapper != null) {
                    UUID sessionId = toSessionId(wrapper.get());
                    if (sessionId != null) {
                        log.debug("[RefreshTokenCache] Cache HIT: sessionId={}", sessionId);
                        return sessionId;
                    }
                    cache.evict(refreshToken);
                    log.warn("[RefreshTokenCache] Invalid cached value evicted");
                }
            }
            log.debug("[RefreshTokenCache] Cache MISS");
        } catch (Exception e) {
            log.warn("[RefreshTokenCache] Failed to get from cache: {}", e.getMessage());
        }
        return null;
    }

    private UUID toSessionId(Object value) {
        if (value instanceof UUID sessionId) {
            return sessionId;
        }
        if (value instanceof String stringValue) {
            try {
                return UUID.fromString(stringValue);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void invalidateToken(String refreshToken) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.evict(refreshToken);
                log.debug("[RefreshTokenCache] Token invalidated from cache");
            }
        } catch (Exception e) {
            log.warn("[RefreshTokenCache] Failed to invalidate token: {}", e.getMessage());
        }
    }

    @Override
    public void invalidateAllByUserId(UUID userId, Set<String> refreshTokens) {
        if (refreshTokens == null || refreshTokens.isEmpty()) {
            log.debug("[RefreshTokenCache] No tokens to invalidate for userId={}", userId);
            return;
        }

        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                for (String token : refreshTokens) {
                    cache.evict(token);
                }
                log.debug("[RefreshTokenCache] {} tokens invalidated for userId={}", refreshTokens.size(), userId);
            }
        } catch (Exception e) {
            log.warn("[RefreshTokenCache] Failed to invalidate tokens for userId={}: {}", userId, e.getMessage());
        }
    }
}

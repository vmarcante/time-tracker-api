package com.vmarcante.time_tracker.core.domain.user.session.service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RefreshTokenCacheService {

    private static final String CACHE_NAME = "refreshTokens";
    private final CacheManager cacheManager;

    public RefreshTokenCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void cacheToken(String refreshToken, UUID sessionId, Duration ttl) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.put(refreshToken, sessionId);
                log.debug("[RefreshTokenCache] Token cached: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.warn("[RefreshTokenCache] Failed to cache token: {}", e.getMessage());
        }
    }

    public UUID getSessionId(String refreshToken) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(refreshToken);
                if (wrapper != null) {
                    UUID sessionId = (UUID) wrapper.get();
                    log.debug("[RefreshTokenCache] Cache HIT: sessionId={}", sessionId);
                    return sessionId;
                }
            }
            log.debug("[RefreshTokenCache] Cache MISS");
        } catch (Exception e) {
            log.warn("[RefreshTokenCache] Failed to get from cache: {}", e.getMessage());
        }
        return null;
    }

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

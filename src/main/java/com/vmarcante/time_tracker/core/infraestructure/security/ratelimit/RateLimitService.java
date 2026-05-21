package com.vmarcante.time_tracker.core.infraestructure.security.ratelimit;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimitService {

    private static final String CACHE_NAME = "rateLimit";
    
    private final CacheManager cacheManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final boolean isRedisCache;

    public RateLimitService(
            CacheManager cacheManager,
            @Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.isRedisCache = isRedisBackedCache();
        
        if (isRedisCache) {
            log.info("[Rate Limit] Using Redis for rate limiting (distributed)");
            return;
        } 
        
        log.warn("[Rate Limit] Using in-memory cache for rate limiting (single instance only - not recommended for production)");
    }

    private boolean isRedisBackedCache() {
        try {
            if (redisTemplate == null) {
                return false;
            }
            Cache cache = cacheManager.getCache(CACHE_NAME);
            return cache instanceof RedisCache;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        if (isRedisCache) {
            return isAllowedRedis(key, maxRequests, windowSeconds);
        }

        return isAllowedInMemory(key, maxRequests, windowSeconds);
    }

    private boolean isAllowedRedis(String key, int maxRequests, int windowSeconds) {
        try {
            String redisKey = "rate_limit:" + key;
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                log.warn("[Rate Limit] Failed to increment counter for key: {}", key);
                return true;
            }

            if (currentCount == 1) {
                redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
            }

            boolean allowed = currentCount <= maxRequests;

            if (!allowed) {
                log.warn("[Rate Limit] Rate limit exceeded | Key: {} | Count: {}/{} | Window: {}s",
                        key, currentCount, maxRequests, windowSeconds);
            } else {
                log.debug("[Rate Limit] Request allowed | Key: {} | Count: {}/{}", 
                        key, currentCount, maxRequests);
            }

            return allowed;

        } catch (Exception e) {
            log.error("[Rate Limit] Error checking rate limit, allowing request", e);
            return true;
        }
    }

    private boolean isAllowedInMemory(String key, int maxRequests, int windowSeconds) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache == null) {
                log.warn("[Rate Limit] Cache not available, allowing request");
                return true;
            }

            String cacheKey = "rate_limit:" + key;
            RateLimitCounter counter = cache.get(cacheKey, RateLimitCounter.class);

            long now = System.currentTimeMillis();
            
            if (counter == null || now > counter.getExpiresAt()) {
                counter = new RateLimitCounter(1, now + (windowSeconds * 1000L));
                cache.put(cacheKey, counter);
                log.debug("[Rate Limit] Request allowed | Key: {} | Count: 1/{}", key, maxRequests);
                return true;
            }

            int currentCount = counter.incrementAndGet();
            cache.put(cacheKey, counter);

            boolean allowed = currentCount <= maxRequests;

            if (!allowed) {
                log.warn("[Rate Limit] Rate limit exceeded | Key: {} | Count: {}/{} | Window: {}s",
                        key, currentCount, maxRequests, windowSeconds);
            } else {
                log.debug("[Rate Limit] Request allowed | Key: {} | Count: {}/{}", 
                        key, currentCount, maxRequests);
            }

            return allowed;

        } catch (Exception e) {
            log.error("[Rate Limit] Error checking rate limit, allowing request", e);
            return true;
        }
    }

    public long getRemainingRequests(String key, int maxRequests) {
        if (isRedisCache) {
            return getRemainingRequestsRedis(key, maxRequests);
        } else {
            return getRemainingRequestsInMemory(key, maxRequests);
        }
    }

    private long getRemainingRequestsRedis(String key, int maxRequests) {
        try {
            String redisKey = "rate_limit:" + key;
            String value = redisTemplate.opsForValue().get(redisKey);
            
            if (value == null) {
                return maxRequests;
            }

            long currentCount = Long.parseLong(value);
            return Math.max(0, maxRequests - currentCount);

        } catch (Exception e) {
            log.error("[Rate Limit] Error getting remaining requests", e);
            return maxRequests;
        }
    }

    private long getRemainingRequestsInMemory(String key, int maxRequests) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache == null) {
                return maxRequests;
            }

            String cacheKey = "rate_limit:" + key;
            RateLimitCounter counter = cache.get(cacheKey, RateLimitCounter.class);

            if (counter == null || System.currentTimeMillis() > counter.getExpiresAt()) {
                return maxRequests;
            }

            return Math.max(0, maxRequests - counter.getCount());

        } catch (Exception e) {
            log.error("[Rate Limit] Error getting remaining requests", e);
            return maxRequests;
        }
    }

    public long getResetTime(String key) {
        if (isRedisCache) {
            return getResetTimeRedis(key);
        } else {
            return getResetTimeInMemory(key);
        }
    }

    private long getResetTimeRedis(String key) {
        try {
            String redisKey = "rate_limit:" + key;
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : 0;

        } catch (Exception e) {
            log.error("[Rate Limit] Error getting reset time", e);
            return 0;
        }
    }

    private long getResetTimeInMemory(String key) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache == null) {
                return 0;
            }

            String cacheKey = "rate_limit:" + key;
            RateLimitCounter counter = cache.get(cacheKey, RateLimitCounter.class);

            if (counter == null) {
                return 0;
            }

            long now = System.currentTimeMillis();
            long resetTime = (counter.getExpiresAt() - now) / 1000;
            return Math.max(0, resetTime);

        } catch (Exception e) {
            log.error("[Rate Limit] Error getting reset time", e);
            return 0;
        }
    }

    public ProgressiveRateLimitResult isAllowedProgressive(String key, ProgressiveLevel[] levels) {
        if (isRedisCache) {
            return isAllowedProgressiveRedis(key, levels);
        }

        return isAllowedProgressiveInMemory(key, levels);
    }

    private int calculateBanAppliedTtl(ProgressiveLevel[] levels) {
        int maxBanSeconds = 0;
        int maxWindowSeconds = 0;
        
        for (ProgressiveLevel level : levels) {
            if (level.banSeconds() > maxBanSeconds) {
                maxBanSeconds = level.banSeconds();
            }
            if (level.windowSeconds() > maxWindowSeconds) {
                maxWindowSeconds = level.windowSeconds();
            }
        }
        
        // TTL = max ban + max window to prevent going back
        return maxBanSeconds + maxWindowSeconds;
    }

    private ProgressiveRateLimitResult isAllowedProgressiveRedis(String key, ProgressiveLevel[] levels) {
        try {
            String banKey = "rate_limit:prog:" + key + ":ban";
            String banAppliedKey = "rate_limit:prog:" + key + ":ban_applied";
            int banAppliedTtl = calculateBanAppliedTtl(levels);

            // Check if banned
            Boolean isBanned = redisTemplate.hasKey(banKey);
            if (isBanned != null && isBanned) {
                Long banTtl = redisTemplate.getExpire(banKey, TimeUnit.SECONDS);
                return new ProgressiveRateLimitResult(false, 0, banTtl != null ? banTtl : 3600, "banned");
            }

            // Check each level progressively
            for (int i = 0; i < levels.length; i++) {
                ProgressiveLevel level = levels[i];
                String levelKey = "rate_limit:prog:" + key + ":l" + i;

                // Check if ban was already applied for this level
                String banApplied = redisTemplate.opsForValue().get(banAppliedKey + ":" + i);
                if (banApplied != null) {
                    // Skip this level, move to next
                    continue;
                }

                Long levelCount = redisTemplate.opsForValue().increment(levelKey);
                if (levelCount == null) {
                    continue;
                }

                if (levelCount == 1) {
                    redisTemplate.expire(levelKey, level.windowSeconds(), TimeUnit.SECONDS);
                }

                if (levelCount > level.maxRequests()) {
                    // Check if this level has a ban
                    if (level.banSeconds() > 0) {
                        // Apply ban and mark it
                        redisTemplate.opsForValue().set(banKey, "1", level.banSeconds(), TimeUnit.SECONDS);
                        redisTemplate.opsForValue().set(banAppliedKey + ":" + i, "1", banAppliedTtl, TimeUnit.SECONDS);
                        return new ProgressiveRateLimitResult(false, 0, level.banSeconds(), "banned");
                    }
                    // Move to next level
                    continue;
                }

                // Request allowed at this level
                return new ProgressiveRateLimitResult(true, level.maxRequests() - levelCount, level.windowSeconds(), "level" + (i + 1));
            }

            // All levels exceeded, ban with default
            redisTemplate.opsForValue().set(banKey, "1", 3600, TimeUnit.SECONDS);
            return new ProgressiveRateLimitResult(false, 0, 3600, "banned");

        } catch (Exception e) {
            log.error("[Rate Limit] Error checking progressive rate limit, allowing request", e);
            return new ProgressiveRateLimitResult(true, levels[0].maxRequests(), 0, "level1");
        }
    }

    private ProgressiveRateLimitResult isAllowedProgressiveInMemory(String key, ProgressiveLevel[] levels) {
        try {
            Cache cache = cacheManager.getCache(CACHE_NAME);
            if (cache == null) {
                log.warn("[Rate Limit] Cache not available, allowing request");
                return new ProgressiveRateLimitResult(true, levels[0].maxRequests(), 0, "level1");
            }

            String banKey = "rate_limit:prog:" + key + ":ban";
            String banAppliedKey = "rate_limit:prog:" + key + ":ban_applied";
            long now = System.currentTimeMillis();
            long banAppliedTtlMs = calculateBanAppliedTtl(levels) * 1000L;

            // Check if banned
            RateLimitCounter banCounter = cache.get(banKey, RateLimitCounter.class);
            if (banCounter != null && now <= banCounter.getExpiresAt()) {
                long banRemaining = (banCounter.getExpiresAt() - now) / 1000;
                return new ProgressiveRateLimitResult(false, 0, banRemaining, "banned");
            }

            // Check each level progressively
            for (int i = 0; i < levels.length; i++) {
                ProgressiveLevel level = levels[i];
                String levelKey = "rate_limit:prog:" + key + ":l" + i;

                // Check if ban was already applied for this level
                RateLimitCounter banAppliedCounter = cache.get(banAppliedKey + ":" + i, RateLimitCounter.class);
                if (banAppliedCounter != null && now <= banAppliedCounter.getExpiresAt()) {
                    // Skip this level, move to next
                    continue;
                }

                RateLimitCounter levelCounter = cache.get(levelKey, RateLimitCounter.class);
                if (levelCounter == null || now > levelCounter.getExpiresAt()) {
                    levelCounter = new RateLimitCounter(1, now + (level.windowSeconds() * 1000L));
                    cache.put(levelKey, levelCounter);
                } else {
                    levelCounter.incrementAndGet();
                    cache.put(levelKey, levelCounter);
                }

                if (levelCounter.getCount() > level.maxRequests()) {
                    // Check if this level has a ban
                    if (level.banSeconds() > 0) {
                        // Apply ban and mark it
                        RateLimitCounter newBan = new RateLimitCounter(1, now + (level.banSeconds() * 1000L));
                        cache.put(banKey, newBan);
                        cache.put(banAppliedKey + ":" + i, new RateLimitCounter(1, now + banAppliedTtlMs));
                        return new ProgressiveRateLimitResult(false, 0, level.banSeconds(), "banned");
                    }
                    // Move to next level
                    continue;
                }

                // Request allowed at this level
                long levelRemaining = (levelCounter.getExpiresAt() - now) / 1000;
                return new ProgressiveRateLimitResult(true, level.maxRequests() - levelCounter.getCount(), levelRemaining, "level" + (i + 1));
            }

            // All levels exceeded, ban with default
            RateLimitCounter newBan = new RateLimitCounter(1, now + 3600000L);
            cache.put(banKey, newBan);
            return new ProgressiveRateLimitResult(false, 0, 3600, "banned");

        } catch (Exception e) {
            log.error("[Rate Limit] Error checking progressive rate limit, allowing request", e);
            return new ProgressiveRateLimitResult(true, levels[0].maxRequests(), 0, "level1");
        }
    }
}

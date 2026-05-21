package com.vmarcante.time_tracker.core.infraestructure.security.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

@Data
class RateLimitCounter {
    
    private AtomicInteger count;
    private long expiresAt;

    public RateLimitCounter(int initialCount, long expiresAt) {
        this.count = new AtomicInteger(initialCount);
        this.expiresAt = expiresAt;
    }

    public int incrementAndGet() {
        return count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}

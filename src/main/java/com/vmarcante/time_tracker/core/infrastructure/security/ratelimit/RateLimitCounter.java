package com.vmarcante.time_tracker.core.infrastructure.security.ratelimit;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class RateLimitCounter {

    private int count;
    private long expiresAt;

    public RateLimitCounter(int initialCount, long expiresAt) {
        this.count = initialCount;
        this.expiresAt = expiresAt;
    }

    public synchronized int incrementAndGet() {
        return ++count;
    }
}

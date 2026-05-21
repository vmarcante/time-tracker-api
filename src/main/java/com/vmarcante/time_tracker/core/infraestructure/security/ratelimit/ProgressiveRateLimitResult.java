package com.vmarcante.time_tracker.core.infraestructure.security.ratelimit;

import lombok.Data;

@Data
public class ProgressiveRateLimitResult {
    private final boolean allowed;
    private final long remainingRequests;
    private final long resetTime;
    private final String level;

    public ProgressiveRateLimitResult(boolean allowed, long remainingRequests, long resetTime, String level) {
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.resetTime = resetTime;
        this.level = level;
    }
}

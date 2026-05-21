package com.vmarcante.time_tracker.core.infraestructure.security.ratelimit.exception;

import com.vmarcante.time_tracker.base.domain.exception.BaseException;

public class RateLimitExceededException extends BaseException {

    private final long retryAfter;

    private static final long ONE_HOUR = 3600;
    private static final long ONE_MINUTE = 60;

    public RateLimitExceededException(long retryAfterSeconds) {
        super(getMessageKey(retryAfterSeconds), formatTime(retryAfterSeconds));
        this.retryAfter = retryAfterSeconds;
    }

    private static String getMessageKey(long seconds) {
        // Multiple hours
        if (seconds >= (2 * ONE_HOUR)) {
            return "rate.limit.exceeded.hours";
        }

        // Single hour
        if (seconds >= ONE_HOUR && seconds < (2 * ONE_HOUR)) {
            return "rate.limit.exceeded.hour";
        }

        // Multiple minutes
        if (seconds >= (2 * ONE_MINUTE)) {
            return "rate.limit.exceeded.minutes";
        }

        // Single minute
        if (seconds >= ONE_MINUTE && seconds < (2 * ONE_MINUTE)) {
            return "rate.limit.exceeded.minute";
        }

        // Multiple seconds
        if (seconds >= 2) {
            return "rate.limit.exceeded.seconds";
        }

        // Single second
        return "rate.limit.exceeded.second";
    }

    private static Object[] formatTime(long seconds) {
        if (seconds >= 3600) {
            long hours = seconds / 3600;
            return new Object[] { hours };
        }

        if (seconds >= 60) {
            long minutes = seconds / 60;
            return new Object[] { minutes };
        }

        return new Object[] { seconds };
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}

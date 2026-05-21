package com.vmarcante.time_tracker.base.domain.exception;

public record ApiErrorResponse(String key, String message) {

    public ApiErrorResponse(String key) {
        this(key, null);
    }
}

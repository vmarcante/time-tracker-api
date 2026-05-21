package com.vmarcante.time_tracker.base.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final String exceptionKey;
    private final Throwable cause; // For logging purpose, stacktrace
    private final Object[] arguments;
    private final HttpStatus customStatus;

    protected BaseException(String exceptionKey) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
        this.cause = null;
        this.arguments = null;
        this.customStatus = null;
    }

    protected BaseException(String exceptionKey, Object... arguments) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
        this.cause = null;
        this.arguments = arguments;
        this.customStatus = null;
    }

    protected BaseException(String exceptionKey, Throwable cause) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
        this.cause = cause;
        this.arguments = null;
        this.customStatus = null;
    }

    protected BaseException(String exceptionKey, Throwable cause, Object... arguments) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
        this.arguments = arguments;
        this.cause = cause;
        this.customStatus = null;
    }

    protected BaseException(String exceptionKey, Throwable cause, HttpStatus customStatus) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
        this.arguments = null;
        this.cause = cause;
        this.customStatus = customStatus;
    }

    protected BaseException(String exceptionKey, Throwable cause, HttpStatus customStatus, Object... arguments) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
        this.arguments = arguments;
        this.cause = cause;
        this.customStatus = customStatus;
    }

}

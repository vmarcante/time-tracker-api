package com.vmarcante.time_tracker.base.domain.exception;

import org.springframework.http.HttpStatus;

public class SecurityException extends BaseException {

    public SecurityException(String exceptionKey, Throwable cause) {
        super(exceptionKey, cause);
    }

    public SecurityException(String exceptionKey, Throwable cause, Object... arguments) {
        super(exceptionKey, cause, arguments);
    }

    public SecurityException(String exceptionKey, Throwable cause, HttpStatus customStatus) {
        super(exceptionKey, cause, customStatus);
    }

    public SecurityException(String exceptionKey, Throwable cause, HttpStatus customStatus, Object... arguments) {
        super(exceptionKey, cause, customStatus, arguments);
    }
}

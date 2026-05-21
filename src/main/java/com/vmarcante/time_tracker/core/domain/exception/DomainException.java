package com.vmarcante.time_tracker.core.domain.exception;

import org.springframework.http.HttpStatus;

import com.vmarcante.time_tracker.base.domain.exception.BaseException;

public class DomainException extends BaseException {

    public DomainException(String exceptionKey, Throwable cause) {
        super(exceptionKey, cause);
    }

    public DomainException(String exceptionKey, Throwable cause, Object... arguments) {
        super(exceptionKey, cause, arguments);
    }

    public DomainException(String exceptionKey, Throwable cause, HttpStatus customStatus) {
        super(exceptionKey, cause, customStatus);
    }

    public DomainException(String exceptionKey, Throwable cause, HttpStatus customStatus, Object... arguments) {
        super(exceptionKey, cause, customStatus, arguments);
    }
}

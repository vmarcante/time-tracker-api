package com.vmarcante.time_tracker.base.domain.exception;

import org.springframework.http.HttpStatus;

public class InfrastructureException extends BaseException {

    public InfrastructureException(String exceptionKey, Throwable cause) {
        super(exceptionKey, cause);
    }

    public InfrastructureException(String exceptionKey, Throwable cause, Object... arguments) {
        super(exceptionKey, cause, arguments);
    }

    public InfrastructureException(String exceptionKey, Throwable cause, HttpStatus customStatus) {
        super(exceptionKey, cause, customStatus);
    }

    public InfrastructureException(String exceptionKey, Throwable cause, HttpStatus customStatus,
            Object... arguments) {
        super(exceptionKey, cause, customStatus, arguments);
    }

}

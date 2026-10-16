package com.vmarcante.time_tracker.base.domain.exception;

import org.springframework.http.HttpStatus;

public class InfraestructureException extends BaseException {

    public InfraestructureException(String exceptionKey, Throwable cause) {
        super(exceptionKey, cause);
    }

    public InfraestructureException(String exceptionKey, Throwable cause, Object... arguments) {
        super(exceptionKey, cause, arguments);
    }

    public InfraestructureException(String exceptionKey, Throwable cause, HttpStatus customStatus) {
        super(exceptionKey, cause, customStatus);
    }

    public InfraestructureException(String exceptionKey, Throwable cause, HttpStatus customStatus,
            Object... arguments) {
        super(exceptionKey, cause, customStatus, arguments);
    }

}

package com.vmarcante.time_tracker.core.application.exception;

import org.springframework.http.HttpStatus;

import com.vmarcante.time_tracker.base.domain.exception.BaseException;

public class ApplicationException extends BaseException {

    public ApplicationException(String exceptionKey, Throwable cause) {
        super(exceptionKey, cause);
    }

    public ApplicationException(String exceptionKey, Throwable cause, Object... arguments) {
        super(exceptionKey, cause, arguments);
    }

    public ApplicationException(String exceptionKey, Throwable cause, HttpStatus customStatus) {
        super(exceptionKey, cause, customStatus);
    }

    public ApplicationException(String exceptionKey, Throwable cause, HttpStatus customStatus,
            Object... arguments) {
        super(exceptionKey, cause, customStatus, arguments);
    }

}

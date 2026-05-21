package com.vmarcante.time_tracker.core.shared.vo.exception;

import com.vmarcante.time_tracker.base.domain.exception.BaseException;

public class VoException extends BaseException {

    public VoException(String exceptionKey) {
        super(exceptionKey);
    }

    public VoException(String exceptionKey, Object... arguments) {
        super(exceptionKey, arguments);
    }
}

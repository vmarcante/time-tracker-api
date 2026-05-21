package com.vmarcante.time_tracker.core.application.email.in;

import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;

public interface RetryFailedEmailUseCase {

    boolean execute(FailedEmail failedEmail);
}

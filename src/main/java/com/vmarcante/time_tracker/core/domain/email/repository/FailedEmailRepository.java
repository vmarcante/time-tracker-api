package com.vmarcante.time_tracker.core.domain.email.repository;

import java.util.List;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;

import java.util.Optional;

public interface FailedEmailRepository {

    Optional<FailedEmail> save(EmailData emailData, Exception exception);

    List<FailedEmail> findAll();

    List<FailedEmail> findAllRetryable(int maxRetryAttempts);

    FailedEmail update(FailedEmail failedEmail);

    void deleteById(UUID id);
}

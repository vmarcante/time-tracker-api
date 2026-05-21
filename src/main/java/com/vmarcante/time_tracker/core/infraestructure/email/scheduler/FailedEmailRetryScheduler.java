package com.vmarcante.time_tracker.core.infraestructure.email.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.email.in.RetryFailedEmailUseCase;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;
import com.vmarcante.time_tracker.core.domain.email.repository.FailedEmailRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FailedEmailRetryScheduler {

    private final int maxRetryAttempts;
    private final FailedEmailRepository failedEmailRepository;
    private final RetryFailedEmailUseCase retryFailedEmailUseCase;

    public FailedEmailRetryScheduler(
            FailedEmailRepository failedEmailRepository,
            RetryFailedEmailUseCase retryFailedEmailUseCase,
            @Value("${email.retry.max.attempts:5}") int maxRetryAttempts) {
        this.failedEmailRepository = failedEmailRepository;
        this.retryFailedEmailUseCase = retryFailedEmailUseCase;
        this.maxRetryAttempts = maxRetryAttempts;
    }

    /**
     * Runs every 30 minutes to retry failed emails
     * Cron expression: At minute 0 and 30 of every hour
     */
    @PostConstruct
    @Scheduled(cron = "${email.retry.cron:0 0/30 * * * *}")
    public void retryFailedEmails() {
        log.info("[Failed Email Retry] Starting scheduled retry job");

        try {
            List<FailedEmail> failedEmails = failedEmailRepository.findAllRetryable(maxRetryAttempts);
            
            if (failedEmails.isEmpty()) {
                log.debug("[Failed Email Retry] No failed emails to retry");
                return;
            }

            log.info("[Failed Email Retry] Found {} failed emails to retry", failedEmails.size());

            int successCount = 0;
            int failedCount = 0;

            for (FailedEmail failedEmail : failedEmails) {
                boolean success = retryFailedEmailUseCase.execute(failedEmail);
                
                if (success) {
                    successCount++;
                } else {
                    failedCount++;
                }
            }

            log.info("[Failed Email Retry] Retry job completed | Success: {} | Failed: {} | Total: {}",
                    successCount, failedCount, failedEmails.size());

        } catch (Exception e) {
            log.error("[Failed Email Retry] Error during retry job execution", e);
        }
    }
}

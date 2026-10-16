package com.vmarcante.time_tracker.core.infraestructure.email.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.vmarcante.time_tracker.core.application.email.in.RetryFailedEmailUseCase;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FailedEmailRetryScheduler {

    private final RetryFailedEmailUseCase retryFailedEmailUseCase;

    public FailedEmailRetryScheduler(RetryFailedEmailUseCase retryFailedEmailUseCase) {
        this.retryFailedEmailUseCase = retryFailedEmailUseCase;
    }

    /**
     * Runs every 30 minutes to retry failed emails
     * Cron expression: At minute 0 and 30 of every hour
     */
    @PostConstruct
    @Scheduled(cron = "${email.retry.cron:0 0/30 * * * *}")
    @SchedulerLock(name = "FailedEmailRetryScheduler.retryFailedEmails", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void retryFailedEmails() {
        log.info("[Failed Email Retry] Starting scheduled retry job");

        try {
            retryFailedEmailUseCase.execute();
        } catch (Exception e) {
            log.error("[Failed Email Retry] Error during retry job execution", e);
        }
    }
}

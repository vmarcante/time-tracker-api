package com.vmarcante.time_tracker.core.infrastructure.user.session.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.user.session.in.DeleteExpiredSessionsUseCase;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SessionCleanupScheduler {

    private final DeleteExpiredSessionsUseCase deleteExpiredSessionsUseCase;

    public SessionCleanupScheduler(DeleteExpiredSessionsUseCase deleteExpiredSessionsUseCase) {
        this.deleteExpiredSessionsUseCase = deleteExpiredSessionsUseCase;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @SchedulerLock(name = "SessionCleanupScheduler.cleanupExpiredSessions", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void cleanupExpiredSessions() {
        log.info("[SessionCleanup] Starting cleanup of expired sessions");
        
        try {
            deleteExpiredSessionsUseCase.execute();
            log.info("[SessionCleanup] Expired sessions cleaned successfully");
        } catch (Exception e) {
            log.error("[SessionCleanup] Error cleaning expired sessions", e);
        }
    }
}

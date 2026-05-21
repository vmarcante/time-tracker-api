package com.vmarcante.time_tracker.core.infraestructure.user.session.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SessionCleanupScheduler {

    private final UserSessionRepository userSessionRepository;

    public SessionCleanupScheduler(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredSessions() {
        log.info("[SessionCleanup] Starting cleanup of expired sessions");
        
        try {
            userSessionRepository.deleteExpiredSessions();
            log.info("[SessionCleanup] Expired sessions cleaned successfully");
        } catch (Exception e) {
            log.error("[SessionCleanup] Error cleaning expired sessions", e);
        }
    }
}

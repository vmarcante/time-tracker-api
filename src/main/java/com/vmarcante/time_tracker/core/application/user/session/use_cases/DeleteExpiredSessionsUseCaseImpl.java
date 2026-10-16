package com.vmarcante.time_tracker.core.application.user.session.use_cases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.user.session.in.DeleteExpiredSessionsUseCase;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;

@Service
public class DeleteExpiredSessionsUseCaseImpl implements DeleteExpiredSessionsUseCase {

    private final UserSessionRepository userSessionRepository;

    public DeleteExpiredSessionsUseCaseImpl(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    @Transactional
    public void execute() {
        userSessionRepository.deleteExpiredSessions();
    }
}

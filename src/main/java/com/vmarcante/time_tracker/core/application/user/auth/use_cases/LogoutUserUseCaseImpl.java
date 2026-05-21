package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.in.LogoutUserUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserContextData;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.session.repository.UserSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogoutUserUseCaseImpl implements LogoutUserUseCase {

    private final SecurityContextPort securityContextPort;
    private final UserSessionRepository userSessionRepository;

    public LogoutUserUseCaseImpl(
            SecurityContextPort securityContextPort,
            UserSessionRepository userSessionRepository) {
        this.securityContextPort = securityContextPort;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    @Transactional
    public void execute() throws ApplicationException {
        Optional<UserContextData> userContextOpt = securityContextPort.getCurrentUser();

        if (userContextOpt.isEmpty()) {
            log.warn("[Logout] No user context found");
            return;
        }

        UserContextData userContext = userContextOpt.get();

        // Invalidar todas as sessões ativas do usuário
        userSessionRepository.invalidateAllByUserId(userContext.userId());

        log.info("[Logout] User logged out successfully | User ID: {} | Username: {}",
                userContext.userId(), userContext.username());

        securityContextPort.clear();
    }
}

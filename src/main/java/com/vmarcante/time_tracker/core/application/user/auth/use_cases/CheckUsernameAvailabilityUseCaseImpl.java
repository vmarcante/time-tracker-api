package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.user.auth.in.CheckUsernameAvailabilityUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

@Service
public class CheckUsernameAvailabilityUseCaseImpl implements CheckUsernameAvailabilityUseCase {

    private final UserAuthRepository userAuthRepository;

    public CheckUsernameAvailabilityUseCaseImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public boolean execute(String username) {
        if (!StringValidationUtils.containsContent(username)) {
            return false;
        }

        String trimmedUsername = username.trim();

        if (trimmedUsername.length() < 3) {
            return false;
        }

        if (trimmedUsername.length() > 30) {
            return false;
        }

        if (!trimmedUsername.matches("^[a-zA-Z0-9._]+$")) {
            return false;
        }

        return !userAuthRepository.existsByUsername(trimmedUsername);
    }
}

package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.ConfirmUserRegistrationInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.ConfirmUserRegistrationUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfirmUserRegistrationUseCaseImpl implements ConfirmUserRegistrationUseCase {

    private final UserAuthRepository userAuthRepository;

    public ConfirmUserRegistrationUseCaseImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    @Transactional
    public void execute(ConfirmUserRegistrationInputDTO input) throws ApplicationException {
        if (!StringValidationUtils.containsContent(input.accessToken())) {
            log.warn("[Confirm Registration] Access token is required");
            throw new ApplicationException("user.access.token.required", null, HttpStatus.BAD_REQUEST);
        }

        String accessToken = input.accessToken().trim();

        Optional<UserAuth> userAuthOpt = userAuthRepository.findByAccessToken(accessToken);
        if (userAuthOpt.isEmpty()) {
            log.warn("[Confirm Registration] Invalid or expired access token");
            throw new ApplicationException("user.access.token.invalid", null, HttpStatus.BAD_REQUEST);
        }
        
        UserAuth userAuth = userAuthOpt.get();

        if (!userAuth.getActive()) {
            log.warn("[Confirm Registration] User is not active | User ID: {}", userAuth.getId());
            throw new ApplicationException("user.inactive", null, HttpStatus.FORBIDDEN);
        }

        if (userAuth.getUserConfirmed()) {
            log.info("[Confirm Registration] User already confirmed | User ID: {}", userAuth.getId());
            throw new ApplicationException("user.already.confirmed", null, HttpStatus.BAD_REQUEST);
        }

        userAuth.setUserConfirmed(true);
        userAuth.setAccessToken(null);

        userAuthRepository.save(userAuth);

        log.info("[Confirm Registration] User registration confirmed successfully | User ID: {} | Username: {}",
                userAuth.getId(), userAuth.getUsername());
    }
}

package com.vmarcante.time_tracker.core.application.user.auth.use_cases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.CreateUserAuthDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CreateUserUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.mapper.UserAuthMapper;
import com.vmarcante.time_tracker.core.application.user.auth.policy.CreateUserInputValidationPolicy;
import com.vmarcante.time_tracker.core.domain.exception.DomainException;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuthCredentials;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.services.GenerateUserAuthCredentialsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserAuthRepository userAuthRepository;
    private final GenerateUserAuthCredentialsService generateCredentialsService;
    private final CreateUserInputValidationPolicy inputValidationPolicy;

    public CreateUserUseCaseImpl(
            UserAuthRepository userAuthRepository,
            GenerateUserAuthCredentialsService generateCredentialsService,
            CreateUserInputValidationPolicy inputValidationPolicy) {
        this.userAuthRepository = userAuthRepository;
        this.generateCredentialsService = generateCredentialsService;
        this.inputValidationPolicy = inputValidationPolicy;
    }

    @Override
    @Transactional
    public UserAuth execute(CreateUserAuthDTO input) throws ApplicationException {
        inputValidationPolicy.validate(input);

        try {
            UserAuthCredentials credentials = generateCredentialsService.execute(input.password());
            UserAuth userAuth = UserAuthMapper.inputToModel(input, credentials);
            UserAuth savedUserAuth = userAuthRepository.save(userAuth);

            return savedUserAuth;
        } catch (DomainException e) {
            throw new ApplicationException(e.getExceptionKey(), e);
        }
    }

}

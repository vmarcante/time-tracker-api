package com.vmarcante.time_tracker.core.application.user.orchestrator;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CurrentUserOutputDTO;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CurrentUserDataOrchestrator {

    private final UserAuthRepository userAuthRepository;
    private final PersonRepository personRepository;
    private final CompanyRepository companyRepository;
    private final SecurityContextPort context;

    public CurrentUserDataOrchestrator(
            UserAuthRepository userAuthRepository,
            PersonRepository personRepository,
            CompanyRepository companyRepository,
            SecurityContextPort context) {
        this.userAuthRepository = userAuthRepository;
        this.personRepository = personRepository;
        this.companyRepository = companyRepository;
        this.context = context;
    }

    public CurrentUserOutputDTO execute() throws ApplicationException {

        Optional<UUID> currentUserId = context.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            log.warn("[Current User Data] User not authenticated");
            throw new ApplicationException("user.authenticated.not", null);
        }

        UUID userId = currentUserId.get();

        Optional<UserAuth> userAuthOptional = userAuthRepository.findById(userId);
        if (userAuthOptional.isEmpty()) {
            log.warn("[Current User Data] User not found");
            throw new ApplicationException("user.not.found", null);
        }

        Optional<Person> personOptional = personRepository.findById(userId);
        if (personOptional.isEmpty()) {
            log.warn("[Current User Data] Person not found");
            throw new ApplicationException("user.not.found", null);
        }

        UserAuth userAuth = userAuthOptional.get();
        Person person = personOptional.get();

        long pendingInvitations = userAuth.getAffiliation() == AffiliationStatus.PENDING
                ? companyRepository.countPendingInvitationsByUserId(userId)
                : 0;

        return new CurrentUserOutputDTO(userAuth, person, pendingInvitations);
    }
}

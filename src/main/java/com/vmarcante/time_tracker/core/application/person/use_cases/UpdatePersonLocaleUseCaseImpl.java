package com.vmarcante.time_tracker.core.application.person.use_cases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonLocaleInputDTO;
import com.vmarcante.time_tracker.core.application.person.in.UpdatePersonLocaleUseCase;
import com.vmarcante.time_tracker.core.domain.person.enums.SupportedLocale;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UpdatePersonLocaleUseCaseImpl implements UpdatePersonLocaleUseCase {

    private final PersonRepository personRepository;
    private final SecurityContextPort context;

    public UpdatePersonLocaleUseCaseImpl(PersonRepository personRepository, SecurityContextPort context) {
        this.personRepository = personRepository;
        this.context = context;
    }

    @Override
    @Transactional
    public void execute(UpdatePersonLocaleInputDTO input) throws ApplicationException {
        Optional<UUID> currentUserId = context.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        if (!SupportedLocale.isSupported(input.locale())) {
            throw new ApplicationException("person.locale.not.supported", null, HttpStatus.BAD_REQUEST,
                    input.locale());
        }

        UUID userId = currentUserId.get();
        String normalizedLocale = SupportedLocale.fromCode(input.locale()).get().getCode();

        personRepository.updateLocale(userId, normalizedLocale);

        log.info("[Update Locale] User {} locale updated to {}", userId, normalizedLocale);
    }
}

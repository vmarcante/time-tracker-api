package com.vmarcante.time_tracker.core.infraestructure.email.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;
import com.vmarcante.time_tracker.core.domain.email.repository.FailedEmailRepository;
import com.vmarcante.time_tracker.core.infraestructure.email.mapper.FailedEmailPersistenceMapper;
import com.vmarcante.time_tracker.core.shared.utils.ExceptionUtils;
import com.vmarcante.time_tracker.core.shared.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FailedEmailPersistenceAdapter implements FailedEmailRepository {

    private final FailedEmailJpaRepository repository;

    public FailedEmailPersistenceAdapter(FailedEmailJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void deleteById(java.util.UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<FailedEmail> findAll() {
        return repository.findAll().stream()
                .map(FailedEmailPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<FailedEmail> findAllRetryable(int maxRetryAttempts) {
        return repository.findAllRetryable(maxRetryAttempts).stream()
                .map(FailedEmailPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public FailedEmail update(FailedEmail failedEmail) {
        FailedEmailJpaEntity entity = FailedEmailPersistenceMapper.toEntity(failedEmail);
        FailedEmailJpaEntity savedEntity = repository.save(entity);
        return FailedEmailPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FailedEmail> save(EmailData emailData, Exception exception) {
        try {
            log.debug("[Failed Email Persistence] Saving failed email to database | Recipient: {} | Subject: {}",
                    emailData.to(), emailData.subject());

            String templateDataJson = JsonUtils.serialize(emailData.templateData());

            String stackTrace = ExceptionUtils.getStackTraceAsString(exception);
            FailedEmailJpaEntity entity = FailedEmailPersistenceMapper.emailDataToEntity(
                    emailData,
                    templateDataJson,
                    exception.getMessage(),
                    stackTrace);
            FailedEmailJpaEntity savedEntity = repository.save(entity);

            log.info("[Failed Email Persistence] Failed email saved successfully | ID: {}", savedEntity.getId());

            return Optional.of(FailedEmailPersistenceMapper.toDomain(savedEntity));

        } catch (Exception e) {
            log.error("[Failed Email Persistence] Critical error saving failed email to database", e);
            throw e;
        }
    }
}

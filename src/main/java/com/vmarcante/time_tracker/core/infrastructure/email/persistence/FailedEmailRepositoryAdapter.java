package com.vmarcante.time_tracker.core.infrastructure.email.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.model.FailedEmail;
import com.vmarcante.time_tracker.core.domain.email.model.filter.FailedEmailFilter;
import com.vmarcante.time_tracker.core.domain.email.repository.FailedEmailRepository;
import com.vmarcante.time_tracker.core.infrastructure.email.mapper.FailedEmailPersistenceMapper;
import com.vmarcante.time_tracker.core.shared.utils.ExceptionUtils;
import com.vmarcante.time_tracker.core.shared.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FailedEmailRepositoryAdapter implements FailedEmailRepository {

    private final FailedEmailJpaRepository repository;

    public FailedEmailRepositoryAdapter(FailedEmailJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void deleteById(UUID id) {
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
        FailedEmailFilter filter = new FailedEmailFilter();
        filter.setMaxRetryCount(maxRetryAttempts);
        return repository.findAll(
                        FailedEmailRepositorySpecification.withFilter(filter),
                        Sort.by(Sort.Direction.ASC, "createdAt"))
                .stream()
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

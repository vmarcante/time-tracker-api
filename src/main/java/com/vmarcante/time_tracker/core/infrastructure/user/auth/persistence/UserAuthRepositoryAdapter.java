package com.vmarcante.time_tracker.core.infrastructure.user.auth.persistence;

import org.springframework.stereotype.Component;
import com.vmarcante.time_tracker.core.domain.user.auth.model.filter.UserAuthFilterMapper;

import com.vmarcante.time_tracker.base.infrastructure.persistence.converter.EncryptedFieldHashUtils;
import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.model.filter.UserAuthFilter;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.infrastructure.user.auth.mapper.UserAuthPersistenceMapper;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserAuthRepositoryAdapter implements UserAuthRepository {

    private final UserAuthJpaRepository jpaRepository;

    public UserAuthRepositoryAdapter(UserAuthJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserAuth save(UserAuth userAuth) {
        UserAuthJpaEntity entity = UserAuthPersistenceMapper.toEntity(userAuth);
        UserAuthJpaEntity savedEntity = jpaRepository.save(entity);
        return UserAuthPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserAuth> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(UserAuthPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserAuth> findByUsername(String username) {
        UserAuthFilter filter = UserAuthFilterMapper.byUsername(username);
        return jpaRepository.findOne(UserAuthRepositorySpecification.withFilter(filter))
                .map(UserAuthPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserAuth> findByAccessToken(String accessToken) {
        return jpaRepository.findByAccessToken(accessToken)
                .map(UserAuthPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        UserAuthFilter filter = UserAuthFilterMapper.byUsername(username);
        return jpaRepository.exists(UserAuthRepositorySpecification.withFilter(filter));
    }

    @Override
    public boolean existsByPersonId(UUID personId) {
        return jpaRepository.existsById(personId);
    }

    @Override
    public Optional<UserAuth> findByUsernameWithPerson(String username) {
        String usernameHash = EncryptedFieldHashUtils.generateSearchHash(username);
        return jpaRepository.findByUsernameHashWithPerson(usernameHash)
                .map(UserAuthPersistenceMapper::toDomain);
    }
}

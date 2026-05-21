package com.vmarcante.time_tracker.core.infraestructure.user.auth.persistence;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.auth.repository.UserAuthRepository;
import com.vmarcante.time_tracker.core.infraestructure.user.auth.mapper.UserAuthPersistenceMapper;

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
        return jpaRepository.findByUsername(username)
                .map(UserAuthPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserAuth> findByAccessToken(String accessToken) {
        return jpaRepository.findByAccessToken(accessToken)
                .map(UserAuthPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public Optional<UserAuth> findByUsernameWithPerson(String username) {
        return jpaRepository.findByUsernameWithPerson(username)
                .map(UserAuthPersistenceMapper::toDomain);
    }
}

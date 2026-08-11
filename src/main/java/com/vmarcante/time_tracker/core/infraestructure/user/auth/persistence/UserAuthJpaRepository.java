package com.vmarcante.time_tracker.core.infraestructure.user.auth.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthJpaRepository
        extends BaseJpaRepository<UserAuthJpaEntity, UUID, Integer> {

    Optional<UserAuthJpaEntity> findByUsernameHash(String usernameHash);

    Optional<UserAuthJpaEntity> findByAccessToken(String accessToken);

    boolean existsByUsernameHash(String usernameHash);

    @Query(" SELECT userAuth FROM UserAuthJpaEntity userAuth " +
           " LEFT JOIN FETCH userAuth.person " +
           " WHERE userAuth.usernameHash = :usernameHash")
    Optional<UserAuthJpaEntity> findByUsernameHashWithPerson(@Param("usernameHash") String usernameHash);
}

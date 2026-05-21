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

    Optional<UserAuthJpaEntity> findByUsername(String username);

    Optional<UserAuthJpaEntity> findByAccessToken(String accessToken);

    boolean existsByUsername(String username);

    @Query(" SELECT userAuth FROM UserAuthJpaEntity userAuth " +
           " LEFT JOIN FETCH userAuth.person " +
           " WHERE userAuth.username = :username")
    Optional<UserAuthJpaEntity> findByUsernameWithPerson(@Param("username") String username);
}

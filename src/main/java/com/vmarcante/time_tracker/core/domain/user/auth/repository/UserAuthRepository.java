package com.vmarcante.time_tracker.core.domain.user.auth.repository;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthRepository {

    UserAuth save(UserAuth userAuth);

    Optional<UserAuth> findById(UUID id);

    Optional<UserAuth> findByUsername(String username);

    Optional<UserAuth> findByAccessToken(String accessToken);

    boolean existsByUsername(String username);

    Optional<UserAuth> findByUsernameWithPerson(String username);
}

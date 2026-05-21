package com.vmarcante.time_tracker.core.infraestructure.user.session.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

@Repository
public interface UserSessionJpaRepository extends BaseJpaRepository<UserSessionJpaEntity, UUID, Integer> {

    Optional<UserSessionJpaEntity> findByRefreshToken(String refreshToken);

    Optional<UserSessionJpaEntity> findByRefreshTokenHashAndActiveTrue(String refreshTokenHash);

    List<UserSessionJpaEntity> findByUserIdAndActiveTrue(UUID userId);

    @Modifying
    @Query("UPDATE UserSessionJpaEntity s SET s.active = false WHERE s.userId = :userId")
    void invalidateAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserSessionJpaEntity s SET s.active = false WHERE s.id = :id")
    void invalidateById(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM UserSessionJpaEntity s WHERE s.refreshTokenExpiry < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);
}

package com.vmarcante.time_tracker.core.infrastructure.user.session.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

@Repository
public interface UserSessionJpaRepository extends BaseJpaRepository<UserSessionJpaEntity, UUID, Integer> {

    Optional<UserSessionJpaEntity> findByRefreshToken(String refreshToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM UserSessionJpaEntity s WHERE s.id = :id")
    Optional<UserSessionJpaEntity> findByIdForUpdate(@Param("id") UUID id);

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

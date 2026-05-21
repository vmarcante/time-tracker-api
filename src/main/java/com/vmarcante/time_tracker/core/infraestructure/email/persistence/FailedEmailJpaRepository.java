package com.vmarcante.time_tracker.core.infraestructure.email.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

@Repository
public interface FailedEmailJpaRepository extends BaseJpaRepository<FailedEmailJpaEntity, UUID, Integer> {

    @Query("SELECT f FROM FailedEmailJpaEntity f ORDER BY f.createdAt ASC")
    List<FailedEmailJpaEntity> findAllPendingEmails();

    @Query("SELECT f FROM FailedEmailJpaEntity f WHERE f.retryCount < :maxRetries ORDER BY f.createdAt ASC")
    List<FailedEmailJpaEntity> findAllRetryable(@Param("maxRetries") int maxRetries);

    @Query("SELECT f FROM FailedEmailJpaEntity f WHERE f.recipient = :recipient ORDER BY f.createdAt DESC")
    List<FailedEmailJpaEntity> findByRecipient(@Param("recipient") String recipient);

    @Query("SELECT f FROM FailedEmailJpaEntity f WHERE f.retryCount >= :maxRetries ORDER BY f.createdAt DESC")
    List<FailedEmailJpaEntity> findExceededMaxRetries(@Param("maxRetries") int maxRetries);
}

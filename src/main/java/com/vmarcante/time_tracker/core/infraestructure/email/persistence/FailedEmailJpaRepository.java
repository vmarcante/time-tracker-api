package com.vmarcante.time_tracker.core.infraestructure.email.persistence;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

@Repository
public interface FailedEmailJpaRepository extends BaseJpaRepository<FailedEmailJpaEntity, UUID, Integer> {
}

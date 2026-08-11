package com.vmarcante.time_tracker.core.infraestructure.person.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

@Repository
public interface PersonJpaRepository
        extends BaseJpaRepository<PersonJpaEntity, UUID, Integer> {

    boolean existsByEmailHash(String emailHash);

    Optional<PersonJpaEntity> findByEmailHash(String emailHash);

}

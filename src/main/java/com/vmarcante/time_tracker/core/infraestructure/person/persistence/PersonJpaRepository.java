package com.vmarcante.time_tracker.core.infraestructure.person.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vmarcante.time_tracker.base.infraestructure.persistence.BaseJpaRepository;

@Repository
public interface PersonJpaRepository
        extends BaseJpaRepository<PersonJpaEntity, UUID, Integer> {

    boolean existsByEmailHash(String emailHash);

    Optional<PersonJpaEntity> findByEmailHash(String emailHash);

    @Query("SELECT p.name FROM PersonJpaEntity p WHERE p.id = :id")
    Optional<String> findNameById(@Param("id") UUID id);

    @Query("SELECT p.id, p.name FROM PersonJpaEntity p WHERE p.id IN :ids")
    List<Object[]> findIdAndNameByIds(@Param("ids") Collection<UUID> ids);

}

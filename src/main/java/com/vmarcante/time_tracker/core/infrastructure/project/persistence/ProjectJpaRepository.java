package com.vmarcante.time_tracker.core.infrastructure.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface ProjectJpaRepository extends BaseJpaRepository<ProjectJpaEntity, UUID, Integer> {

    @Query("""
            SELECT p.id, p.name FROM ProjectJpaEntity p
            WHERE p.id IN :ids
            """)
    List<Object[]> findIdAndNameByIds(@Param("ids") Collection<UUID> ids);
}

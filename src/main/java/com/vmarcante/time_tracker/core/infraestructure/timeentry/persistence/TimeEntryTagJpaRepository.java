package com.vmarcante.time_tracker.core.infraestructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeEntryTagJpaRepository extends JpaRepository<TimeEntryTagJpaEntity, UUID> {

    List<TimeEntryTagJpaEntity> findByEntryIdAndActiveTrue(UUID entryId);

    List<TimeEntryTagJpaEntity> findByTagIdAndActiveTrue(UUID tagId);

    @Query("SELECT et.entryId, t FROM TimeEntryTagJpaEntity et JOIN TagJpaEntity t ON t.id = et.tagId "
            + "WHERE et.entryId IN :entryIds AND et.active = true AND t.active = true")
    List<Object[]> findEntryIdAndTagByEntryIds(@Param("entryIds") Collection<UUID> entryIds);

    @Query("SELECT et FROM TimeEntryTagJpaEntity et WHERE et.entryId = :entryId AND et.tagId IN :tagIds "
            + "AND et.active = true")
    List<TimeEntryTagJpaEntity> findActiveByEntryIdAndTagIds(@Param("entryId") UUID entryId,
            @Param("tagIds") Collection<UUID> tagIds);
}

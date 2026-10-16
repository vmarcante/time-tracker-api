package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface TimeEntryTagJpaRepository extends BaseJpaRepository<TimeEntryTagJpaEntity, UUID, Integer> {

    @Query("SELECT et.entryId, t FROM TimeEntryTagJpaEntity et JOIN TagJpaEntity t ON t.id = et.tagId "
            + "WHERE et.entryId IN :entryIds AND et.active = true AND t.active = true")
    List<Object[]> findEntryIdAndTagByEntryIds(@Param("entryIds") Collection<UUID> entryIds);
}

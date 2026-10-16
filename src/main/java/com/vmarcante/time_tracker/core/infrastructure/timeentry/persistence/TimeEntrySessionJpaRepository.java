package com.vmarcante.time_tracker.core.infrastructure.timeentry.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vmarcante.time_tracker.base.infrastructure.persistence.BaseJpaRepository;

public interface TimeEntrySessionJpaRepository extends BaseJpaRepository<TimeEntrySessionJpaEntity, UUID, Integer> {

    @Query(value = "SELECT CAST(s.C0013_START_TIME AS date) AS day, "
            + "SUM(EXTRACT(EPOCH FROM (COALESCE(s.C0013_END_TIME, LOCALTIMESTAMP) - s.C0013_START_TIME)))::bigint AS seconds "
            + "FROM dbo.TB0013_TIME_ENTRY_SESSION s "
            + "WHERE s.C0013_USER_ID = :userId AND s.C0013_ACTIVE = true "
            + "AND s.C0013_START_TIME >= :from AND s.C0013_START_TIME < :toExclusive "
            + "GROUP BY CAST(s.C0013_START_TIME AS date)", nativeQuery = true)
    List<Object[]> sumSecondsByDay(
            @Param("userId") UUID userId,
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive);
}

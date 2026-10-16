package com.vmarcante.time_tracker.core.application.timeentry.entry.dto.input;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import com.vmarcante.time_tracker.base.application.dto.input.BaseInputFilterDTO;
import com.vmarcante.time_tracker.core.application.timeentry.entry.enums.TimeEntrySortType;
import com.vmarcante.time_tracker.core.domain.timeentry.model.filter.TimeEntryFilter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyTimeEntriesFilterInputDTO extends BaseInputFilterDTO<TimeEntrySortType> {

    private List<UUID> userIds;
    private List<UUID> teamIds;
    private UUID projectId;
    private String name;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    public TimeEntryFilter toDomainFilter() {
        TimeEntryFilter filter = new TimeEntryFilter();
        filter.setUserIds(userIds);
        filter.setTeamIds(teamIds);
        filter.setProjectId(projectId);
        filter.setName(name);
        filter.setFrom(from);
        filter.setTo(to);
        return filter;
    }
}

package com.vmarcante.time_tracker.core.application.timeentry.in;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;

public interface ListCompanyTimeEntriesUseCase {

    Page<TimeEntrySummaryOutputDTO> execute(UUID companyId, UUID userId, UUID projectId,
            LocalDateTime from, LocalDateTime to, Pageable pageable) throws ApplicationException;
}

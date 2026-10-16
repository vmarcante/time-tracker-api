package com.vmarcante.time_tracker.core.interfaces.timeentry.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.CompanyTimeEntriesFilterInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.CompanyTimeEntryStatsOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListCompanyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListCompanyTimeEntryStatsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/time-entries")
@Tag(name = "Company Time Entries", description = "Company-scoped time entry listing with hierarchy visibility")
public class CompanyTimeEntryController extends BaseResponseController {

    private final ListCompanyTimeEntriesUseCase listCompanyTimeEntriesUseCase;
    private final ListCompanyTimeEntryStatsUseCase listCompanyTimeEntryStatsUseCase;

    public CompanyTimeEntryController(ListCompanyTimeEntriesUseCase listCompanyTimeEntriesUseCase,
            ListCompanyTimeEntryStatsUseCase listCompanyTimeEntryStatsUseCase) {
        this.listCompanyTimeEntriesUseCase = listCompanyTimeEntriesUseCase;
        this.listCompanyTimeEntryStatsUseCase = listCompanyTimeEntryStatsUseCase;
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List company time entries",
            description = "Lists entries visible to the actor: all for managers, own + led-team scope for members")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<TimeEntrySummaryOutputDTO>>> list(
            @PathVariable UUID companyId,
            CompanyTimeEntriesFilterInputDTO filter) throws ApplicationException {
        return ok(listCompanyTimeEntriesUseCase.execute(companyId, filter));
    }

    @AuthSecure
    @GetMapping("/stats")
    @Operation(summary = "Company time entry statistics",
            description = "Aggregated stats (totals, per member, per project, per day) within the actor's visibility scope")
    public ResponseEntity<ApiResponseDTO<CompanyTimeEntryStatsOutputDTO>> stats(
            @PathVariable UUID companyId,
            CompanyTimeEntriesFilterInputDTO filter)
            throws ApplicationException {
        return ok(listCompanyTimeEntryStatsUseCase.execute(companyId, filter));
    }
}

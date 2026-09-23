package com.vmarcante.time_tracker.core.interfaces.timeentry.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListCompanyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies/{companyId}/time-entries")
@Tag(name = "Company Time Entries", description = "Company-scoped time entry listing with hierarchy visibility")
public class CompanyTimeEntryController extends BaseResponseController {

    private final ListCompanyTimeEntriesUseCase listCompanyTimeEntriesUseCase;

    public CompanyTimeEntryController(ListCompanyTimeEntriesUseCase listCompanyTimeEntriesUseCase) {
        this.listCompanyTimeEntriesUseCase = listCompanyTimeEntriesUseCase;
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List company time entries",
            description = "Lists entries visible to the actor: all for managers, own + led-team scope for members")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<TimeEntrySummaryOutputDTO>>> list(
            @PathVariable UUID companyId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(
                listCompanyTimeEntriesUseCase.execute(companyId, userId, projectId, from, to, pageable)));
    }
}

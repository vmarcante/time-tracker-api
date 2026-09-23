package com.vmarcante.time_tracker.core.interfaces.timeentry.controllers;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.CreateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.SetTimeEntryTagsInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.UpdateTimeEntryInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntrySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.CreateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.DeactivateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.FindTimeEntryByIdUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.ListMyTimeEntriesUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.SetTimeEntryTagsUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.UpdateTimeEntryUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;
import com.vmarcante.time_tracker.core.shared.utils.PageableUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/time-entries")
@Tag(name = "Time Entries", description = "Time entry management endpoints")
public class TimeEntryController extends BaseResponseController {

    private final CreateTimeEntryUseCase createTimeEntryUseCase;
    private final UpdateTimeEntryUseCase updateTimeEntryUseCase;
    private final DeactivateTimeEntryUseCase deactivateTimeEntryUseCase;
    private final FindTimeEntryByIdUseCase findTimeEntryByIdUseCase;
    private final ListMyTimeEntriesUseCase listMyTimeEntriesUseCase;
    private final SetTimeEntryTagsUseCase setTimeEntryTagsUseCase;

    public TimeEntryController(
            CreateTimeEntryUseCase createTimeEntryUseCase,
            UpdateTimeEntryUseCase updateTimeEntryUseCase,
            DeactivateTimeEntryUseCase deactivateTimeEntryUseCase,
            FindTimeEntryByIdUseCase findTimeEntryByIdUseCase,
            ListMyTimeEntriesUseCase listMyTimeEntriesUseCase,
            SetTimeEntryTagsUseCase setTimeEntryTagsUseCase) {
        this.createTimeEntryUseCase = createTimeEntryUseCase;
        this.updateTimeEntryUseCase = updateTimeEntryUseCase;
        this.deactivateTimeEntryUseCase = deactivateTimeEntryUseCase;
        this.findTimeEntryByIdUseCase = findTimeEntryByIdUseCase;
        this.listMyTimeEntriesUseCase = listMyTimeEntriesUseCase;
        this.setTimeEntryTagsUseCase = setTimeEntryTagsUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Create time entry", description = "Creates a time entry on an assigned or personal project")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> create(
            @RequestBody CreateTimeEntryInputDTO input) throws ApplicationException {
        return created(createTimeEntryUseCase.execute(input));
    }

    @AuthSecure
    @GetMapping
    @Operation(summary = "List my time entries", description = "Lists the authenticated user's time entries with optional filters")
    public ResponseEntity<ApiResponseDTO<PageWrapperDTO<TimeEntrySummaryOutputDTO>>> list(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) throws ApplicationException {
        Pageable pageable = PageableUtils.pageable(page, size, null);
        return ok(PageWrapperDTO.of(listMyTimeEntriesUseCase.execute(projectId, from, to, pageable)));
    }

    @AuthSecure
    @GetMapping("/{entryId}")
    @Operation(summary = "Get time entry", description = "Returns a time entry detail with sessions and tags")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> findById(
            @PathVariable UUID entryId) throws ApplicationException {
        return ok(findTimeEntryByIdUseCase.execute(entryId));
    }

    @AuthSecure
    @PutMapping("/{entryId}")
    @Operation(summary = "Update time entry", description = "Updates a time entry owned by the authenticated user")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> update(
            @PathVariable UUID entryId,
            @RequestBody UpdateTimeEntryInputDTO input) throws ApplicationException {
        return ok(updateTimeEntryUseCase.execute(entryId, input));
    }

    @AuthSecure
    @DeleteMapping("/{entryId}")
    @Operation(summary = "Deactivate time entry", description = "Deactivates a time entry and its sessions")
    public ResponseEntity<ApiResponseDTO<Void>> deactivate(
            @PathVariable UUID entryId) throws ApplicationException {
        deactivateTimeEntryUseCase.execute(entryId);
        return noContent();
    }

    @AuthSecure
    @PutMapping("/{entryId}/tags")
    @Operation(summary = "Set entry tags", description = "Replaces the tag set of a time entry")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> setTags(
            @PathVariable UUID entryId,
            @RequestBody SetTimeEntryTagsInputDTO input) throws ApplicationException {
        return ok(setTimeEntryTagsUseCase.execute(entryId, input));
    }
}

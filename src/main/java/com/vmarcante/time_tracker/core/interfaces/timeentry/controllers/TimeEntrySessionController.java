package com.vmarcante.time_tracker.core.interfaces.timeentry.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.SessionInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.CreateSessionUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.DeleteSessionUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.UpdateSessionUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/time-entries/{entryId}/sessions")
@Tag(name = "Time Entry Sessions", description = "Time block management inside time entries")
public class TimeEntrySessionController extends BaseResponseController {

    private final CreateSessionUseCase createSessionUseCase;
    private final UpdateSessionUseCase updateSessionUseCase;
    private final DeleteSessionUseCase deleteSessionUseCase;

    public TimeEntrySessionController(
            CreateSessionUseCase createSessionUseCase,
            UpdateSessionUseCase updateSessionUseCase,
            DeleteSessionUseCase deleteSessionUseCase) {
        this.createSessionUseCase = createSessionUseCase;
        this.updateSessionUseCase = updateSessionUseCase;
        this.deleteSessionUseCase = deleteSessionUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Add session", description = "Adds a manual time block to the entry (endTime null starts a timer)")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> create(
            @PathVariable UUID entryId,
            @RequestBody SessionInputDTO input) throws ApplicationException {
        return created(createSessionUseCase.execute(entryId, input));
    }

    @AuthSecure
    @PutMapping("/{sessionId}")
    @Operation(summary = "Update session", description = "Updates a session time range and description")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> update(
            @PathVariable UUID entryId,
            @PathVariable UUID sessionId,
            @RequestBody SessionInputDTO input) throws ApplicationException {
        return ok(updateSessionUseCase.execute(entryId, sessionId, input));
    }

    @AuthSecure
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete session", description = "Removes a time block from the entry")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> delete(
            @PathVariable UUID entryId,
            @PathVariable UUID sessionId) throws ApplicationException {
        return ok(deleteSessionUseCase.execute(entryId, sessionId));
    }
}

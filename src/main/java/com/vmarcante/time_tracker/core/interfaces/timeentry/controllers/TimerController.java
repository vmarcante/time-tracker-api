package com.vmarcante.time_tracker.core.interfaces.timeentry.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.timeentry.dto.input.StartTimerInputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.RunningTimerOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.dto.output.TimeEntryDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.timeentry.in.GetRunningTimerUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.StartTimerUseCase;
import com.vmarcante.time_tracker.core.application.timeentry.in.StopTimerUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/timer")
@Tag(name = "Timer", description = "Real-time timer endpoints")
public class TimerController extends BaseResponseController {

    private final StartTimerUseCase startTimerUseCase;
    private final StopTimerUseCase stopTimerUseCase;
    private final GetRunningTimerUseCase getRunningTimerUseCase;

    public TimerController(
            StartTimerUseCase startTimerUseCase,
            StopTimerUseCase stopTimerUseCase,
            GetRunningTimerUseCase getRunningTimerUseCase) {
        this.startTimerUseCase = startTimerUseCase;
        this.stopTimerUseCase = stopTimerUseCase;
        this.getRunningTimerUseCase = getRunningTimerUseCase;
    }

    @AuthSecure
    @PostMapping("/start")
    @Operation(summary = "Start timer", description = "Starts a timer on an existing entry or creates a quick entry from projectId")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> start(
            @RequestBody StartTimerInputDTO input) throws ApplicationException {
        return ok(startTimerUseCase.execute(input));
    }

    @AuthSecure
    @PostMapping("/stop")
    @Operation(summary = "Stop timer", description = "Stops the authenticated user's running timer")
    public ResponseEntity<ApiResponseDTO<TimeEntryDetailOutputDTO>> stop() throws ApplicationException {
        return ok(stopTimerUseCase.execute());
    }

    @AuthSecure
    @GetMapping("/current")
    @Operation(summary = "Current timer", description = "Returns the running timer or null")
    public ResponseEntity<ApiResponseDTO<RunningTimerOutputDTO>> current() throws ApplicationException {
        return ok(getRunningTimerUseCase.execute());
    }
}

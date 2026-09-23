package com.vmarcante.time_tracker.core.interfaces.person.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CurrentUserOutputDTO;
import com.vmarcante.time_tracker.core.application.user.orchestrator.CurrentUserDataOrchestrator;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/person")
@Tag(name = "Person Management", description = "Person management private endpoints")
public class PersonController extends BaseResponseController {

    private final CurrentUserDataOrchestrator currentUserDataOrchestrator;

    public PersonController(CurrentUserDataOrchestrator currentUserDataOrchestrator) {
        this.currentUserDataOrchestrator = currentUserDataOrchestrator;
    }

    @AuthSecure(allowPendingOnboarding = true)
    @GetMapping("/me")
    @Operation(summary = "Get current user data", description = "Returns the authenticated user's data")
    public ResponseEntity<ApiResponseDTO<CurrentUserOutputDTO>> getCurrentUser() {
        CurrentUserOutputDTO currentUserData = currentUserDataOrchestrator.execute();
        return ok(currentUserData);
    }

}

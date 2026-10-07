package com.vmarcante.time_tracker.core.interfaces.person.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonLocaleInputDTO;
import com.vmarcante.time_tracker.core.application.person.dto.UpdatePersonProfileInputDTO;
import com.vmarcante.time_tracker.core.application.person.in.UpdatePersonLocaleUseCase;
import com.vmarcante.time_tracker.core.application.person.in.UpdatePersonProfileUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CurrentUserOutputDTO;
import com.vmarcante.time_tracker.core.application.user.orchestrator.CurrentUserDataOrchestrator;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/person")
@Tag(name = "Person Management", description = "Person management private endpoints")
public class PersonController extends BaseResponseController {

    private final CurrentUserDataOrchestrator currentUserDataOrchestrator;
    private final UpdatePersonLocaleUseCase updatePersonLocaleUseCase;
    private final UpdatePersonProfileUseCase updatePersonProfileUseCase;

    public PersonController(
            CurrentUserDataOrchestrator currentUserDataOrchestrator,
            UpdatePersonLocaleUseCase updatePersonLocaleUseCase,
            UpdatePersonProfileUseCase updatePersonProfileUseCase) {
        this.currentUserDataOrchestrator = currentUserDataOrchestrator;
        this.updatePersonLocaleUseCase = updatePersonLocaleUseCase;
        this.updatePersonProfileUseCase = updatePersonProfileUseCase;
    }

    @AuthSecure(allowPendingOnboarding = true)
    @GetMapping("/me")
    @Operation(summary = "Get current user data", description = "Returns the authenticated user's data")
    public ResponseEntity<ApiResponseDTO<CurrentUserOutputDTO>> getCurrentUser() {
        CurrentUserOutputDTO currentUserData = currentUserDataOrchestrator.execute();
        return ok(currentUserData);
    }

    @AuthSecure(allowPendingOnboarding = true)
    @PatchMapping("/me/locale")
    @Operation(
            summary = "Update user locale",
            description = "Updates the authenticated user's preferred locale (e.g. pt-BR, en-US)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Locale updated successfully"),
            @ApiResponse(responseCode = "400", description = "Unsupported locale code"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponseDTO<Void>> updateLocale(
            @RequestBody UpdatePersonLocaleInputDTO input) throws ApplicationException {
        updatePersonLocaleUseCase.execute(input);
        return noContent();
    }

    @AuthSecure
    @PatchMapping("/me/profile")
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's name, phone and age")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponseDTO<Void>> updateProfile(
            @RequestBody UpdatePersonProfileInputDTO input) throws ApplicationException {
        updatePersonProfileUseCase.execute(input);
        return noContent();
    }
}

package com.vmarcante.time_tracker.core.interfaces.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.OnboardingInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CompleteOnboardingOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CompleteOnboardingUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/onboarding")
@Tag(name = "Onboarding", description = "User affiliation onboarding endpoints")
public class OnboardingController extends BaseResponseController {

    private final CompleteOnboardingUseCase completeOnboardingUseCase;

    public OnboardingController(CompleteOnboardingUseCase completeOnboardingUseCase) {
        this.completeOnboardingUseCase = completeOnboardingUseCase;
    }

    @AuthSecure(allowPendingOnboarding = true)
    @PostMapping
    @Operation(
            summary = "Complete onboarding",
            description = "Defines the user's affiliation: a company document (CNPJ) to join a company, "
                    + "or 'independent: true' to proceed without a company")
    public ResponseEntity<ApiResponseDTO<CompleteOnboardingOutputDTO>> complete(
            @RequestBody OnboardingInputDTO input) {
        return ok(completeOnboardingUseCase.execute(input));
    }
}

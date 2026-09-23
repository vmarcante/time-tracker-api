package com.vmarcante.time_tracker.core.interfaces.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyPreviewOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.FindCompanyByDocumentUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.OnboardingCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.OnboardingInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CompleteOnboardingOutputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.in.CompleteOnboardingUseCase;
import com.vmarcante.time_tracker.core.application.user.auth.in.CreateOnboardingCompanyUseCase;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/onboarding")
@Tag(name = "Onboarding", description = "User affiliation onboarding endpoints")
public class OnboardingController extends BaseResponseController {

    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final CreateOnboardingCompanyUseCase createOnboardingCompanyUseCase;
    private final FindCompanyByDocumentUseCase findCompanyByDocumentUseCase;

    public OnboardingController(
            CompleteOnboardingUseCase completeOnboardingUseCase,
            CreateOnboardingCompanyUseCase createOnboardingCompanyUseCase,
            FindCompanyByDocumentUseCase findCompanyByDocumentUseCase) {
        this.completeOnboardingUseCase = completeOnboardingUseCase;
        this.createOnboardingCompanyUseCase = createOnboardingCompanyUseCase;
        this.findCompanyByDocumentUseCase = findCompanyByDocumentUseCase;
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

    @AuthSecure(allowPendingOnboarding = true)
    @PostMapping("/company")
    @Operation(
            summary = "Register company during onboarding",
            description = "Creates a new company and assigns the pending user as OWNER. "
                    + "Only available while onboarding is pending")
    public ResponseEntity<ApiResponseDTO<CompleteOnboardingOutputDTO>> createCompany(
            @RequestBody OnboardingCompanyInputDTO input) {
        return ok(createOnboardingCompanyUseCase.execute(input));
    }

    @AuthSecure(allowPendingOnboarding = true)
    @GetMapping("/company-preview/{document}")
    @Operation(
            summary = "Company preview by document",
            description = "Returns company display data by CNPJ for the membership link flow. "
                    + "Only available while onboarding is pending")
    public ResponseEntity<ApiResponseDTO<CompanyPreviewOutputDTO>> companyPreview(
            @PathVariable String document) {
        return ok(findCompanyByDocumentUseCase.execute(document));
    }
}

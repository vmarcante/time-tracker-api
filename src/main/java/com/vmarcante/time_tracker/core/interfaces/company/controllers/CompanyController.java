package com.vmarcante.time_tracker.core.interfaces.company.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;
import com.vmarcante.time_tracker.base.interfaces.controllers.BaseResponseController;
import com.vmarcante.time_tracker.core.application.company.dto.input.CreateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.UpdateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyDetailOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CreateCompanyOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.CreateCompanyUseCase;
import com.vmarcante.time_tracker.core.application.company.in.DeactivateCompanyUseCase;
import com.vmarcante.time_tracker.core.application.company.in.FindCompanyByIdUseCase;
import com.vmarcante.time_tracker.core.application.company.in.FindUserCompaniesUseCase;
import com.vmarcante.time_tracker.core.application.company.in.UpdateCompanyUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.user.auth.annotation.AuthSecure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/companies")
@Tag(name = "Company Management", description = "Company management endpoints")
public class CompanyController extends BaseResponseController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final FindCompanyByIdUseCase findCompanyByIdUseCase;
    private final FindUserCompaniesUseCase findUserCompaniesUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeactivateCompanyUseCase deactivateCompanyUseCase;

    public CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            FindCompanyByIdUseCase findCompanyByIdUseCase,
            FindUserCompaniesUseCase findUserCompaniesUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeactivateCompanyUseCase deactivateCompanyUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.findCompanyByIdUseCase = findCompanyByIdUseCase;
        this.findUserCompaniesUseCase = findUserCompaniesUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deactivateCompanyUseCase = deactivateCompanyUseCase;
    }

    @AuthSecure
    @PostMapping
    @Operation(summary = "Create company", description = "Creates a new company and assigns the authenticated user as OWNER")
    public ResponseEntity<ApiResponseDTO<CreateCompanyOutputDTO>> createCompany(
            @RequestBody CreateCompanyInputDTO input) throws ApplicationException {
        return created(createCompanyUseCase.execute(input));
    }

    @AuthSecure
    @GetMapping("/me")
    @Operation(summary = "My companies", description = "Returns all active companies the authenticated user belongs to")
    public ResponseEntity<ApiResponseDTO<List<CompanySummaryOutputDTO>>> myCompanies() throws ApplicationException {
        return ok(findUserCompaniesUseCase.execute());
    }

    @AuthSecure
    @GetMapping("/{id}")
    @Operation(summary = "Find company by ID", description = "Returns a company by its ID (members only)")
    public ResponseEntity<ApiResponseDTO<CompanyDetailOutputDTO>> findCompany(
            @PathVariable UUID id) throws ApplicationException {
        return ok(findCompanyByIdUseCase.execute(id));
    }

    @AuthSecure
    @PutMapping("/{id}")
    @Operation(summary = "Update company", description = "Updates company legal name, trade name, description or timezone (OWNER or MANAGER only)")
    public ResponseEntity<ApiResponseDTO<CompanyDetailOutputDTO>> updateCompany(
            @PathVariable UUID id,
            @RequestBody UpdateCompanyInputDTO input) throws ApplicationException {
        return ok(updateCompanyUseCase.execute(id, input));
    }

    @AuthSecure
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate company", description = "Soft-deletes the company (OWNER only)")
    public ResponseEntity<ApiResponseDTO<Void>> deactivateCompany(
            @PathVariable UUID id) throws ApplicationException {
        deactivateCompanyUseCase.execute(id);
        return noContent();
    }
}

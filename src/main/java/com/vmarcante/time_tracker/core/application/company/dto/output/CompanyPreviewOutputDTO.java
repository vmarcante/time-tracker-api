package com.vmarcante.time_tracker.core.application.company.dto.output;

import com.vmarcante.time_tracker.core.domain.company.model.Company;

public record CompanyPreviewOutputDTO(
        String legalName,
        String tradeName) {

    public static CompanyPreviewOutputDTO from(Company company) {
        return new CompanyPreviewOutputDTO(
                company.getLegalName(),
                company.getTradeName());
    }
}

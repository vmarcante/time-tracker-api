package com.vmarcante.time_tracker.core.application.company.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.model.Company;

public record CompanySummaryOutputDTO(
        UUID id,
        String legalName,
        String tradeName,
        Boolean active) {

    public static CompanySummaryOutputDTO from(Company company) {
        return new CompanySummaryOutputDTO(
                company.getId(),
                company.getLegalName(),
                company.getTradeName(),
                company.getActive());
    }
}

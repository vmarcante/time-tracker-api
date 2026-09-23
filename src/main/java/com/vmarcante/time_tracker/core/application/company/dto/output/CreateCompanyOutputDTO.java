package com.vmarcante.time_tracker.core.application.company.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.model.Company;

public record CreateCompanyOutputDTO(
        UUID id,
        String legalName,
        String tradeName,
        String document,
        String timezone,
        LocalDateTime createdAt) {

    public static CreateCompanyOutputDTO from(Company company) {
        return new CreateCompanyOutputDTO(
                company.getId(),
                company.getLegalName(),
                company.getTradeName(),
                company.getDocument(),
                company.getTimezone(),
                company.getCreatedAt());
    }
}

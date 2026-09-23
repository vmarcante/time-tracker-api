package com.vmarcante.time_tracker.core.application.company.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.company.model.Company;

public record CompanyDetailOutputDTO(
        UUID id,
        String legalName,
        String tradeName,
        String document,
        String description,
        String timezone,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CompanyDetailOutputDTO from(Company company) {
        return new CompanyDetailOutputDTO(
                company.getId(),
                company.getLegalName(),
                company.getTradeName(),
                company.getDocument(),
                company.getDescription(),
                company.getTimezone(),
                company.getActive(),
                company.getCreatedAt(),
                company.getUpdatedAt());
    }
}

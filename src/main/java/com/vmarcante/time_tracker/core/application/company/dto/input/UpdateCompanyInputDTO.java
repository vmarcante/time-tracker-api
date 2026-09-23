package com.vmarcante.time_tracker.core.application.company.dto.input;

public record UpdateCompanyInputDTO(
        String legalName,
        String tradeName,
        String description,
        String timezone) {
}

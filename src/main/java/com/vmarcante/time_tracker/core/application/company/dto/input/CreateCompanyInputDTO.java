package com.vmarcante.time_tracker.core.application.company.dto.input;

import com.vmarcante.time_tracker.core.shared.vo.Cnpj;

public record CreateCompanyInputDTO(
        String legalName,
        String tradeName,
        Cnpj document,
        String description,
        String timezone) {
}

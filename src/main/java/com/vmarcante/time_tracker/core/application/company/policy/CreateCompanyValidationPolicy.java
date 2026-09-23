package com.vmarcante.time_tracker.core.application.company.policy;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.application.company.dto.input.CreateCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;

@Component
public class CreateCompanyValidationPolicy {

    public void validate(CreateCompanyInputDTO input) throws ApplicationException {
        if (input.legalName() == null || input.legalName().isBlank()) {
            throw new ApplicationException("company.legal.name.required", null);
        }

        String trimmedLegalName = input.legalName().trim();
        if (trimmedLegalName.length() < 2) {
            throw new ApplicationException("company.legal.name.min.length", null);
        }
        if (trimmedLegalName.length() > 200) {
            throw new ApplicationException("company.legal.name.max.length", null);
        }
    }
}

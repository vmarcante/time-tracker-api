package com.vmarcante.time_tracker.core.application.user.auth.in;

import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.application.user.auth.dto.input.OnboardingCompanyInputDTO;
import com.vmarcante.time_tracker.core.application.user.auth.dto.output.CompleteOnboardingOutputDTO;

public interface CreateOnboardingCompanyUseCase {

    CompleteOnboardingOutputDTO execute(OnboardingCompanyInputDTO input) throws ApplicationException;
}

package com.vmarcante.time_tracker.core.application.user.auth.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;

public record CompleteOnboardingOutputDTO(
        AffiliationStatus affiliation,
        UUID companyId,
        String companyName,
        Boolean membershipPending) {

    public static CompleteOnboardingOutputDTO independent() {
        return new CompleteOnboardingOutputDTO(AffiliationStatus.INDEPENDENT, null, null, null);
    }

    public static CompleteOnboardingOutputDTO company(
            UUID companyId, String companyName, Boolean membershipPending) {
        return new CompleteOnboardingOutputDTO(
                AffiliationStatus.COMPANY, companyId, companyName, membershipPending);
    }
}

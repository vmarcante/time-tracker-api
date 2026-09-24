package com.vmarcante.time_tracker.core.application.user.auth.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;

public record CurrentUserOutputDTO(
        UUID id,
        String username,
        String name,
        UserRoleType role,
        AffiliationStatus affiliation,
        String email,
        String phone,
        String locale,
        long pendingInvitations,
        String pendingCompanyName) {

    public CurrentUserOutputDTO(UserAuth auth, Person person, long pendingInvitations, String pendingCompanyName) {
        this(
                auth.getId(),
                auth.getUsername(),
                person.getName(),
                auth.getRole(),
                auth.getAffiliation(),
                person.getEmail().address(),
                person.getPhone().number(),
                person.getLocale(),
                pendingInvitations,
                pendingCompanyName);
    }

}
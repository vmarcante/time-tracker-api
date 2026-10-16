package com.vmarcante.time_tracker.core.application.company.membership.dto.output;

import java.util.UUID;

public record PersonInviteLookupOutputDTO(
        PersonInviteLookupStatus status,
        UUID personId,
        String name,
        String email) {

    public static PersonInviteLookupOutputDTO notFound() {
        return new PersonInviteLookupOutputDTO(PersonInviteLookupStatus.NOT_FOUND, null, null, null);
    }

    public static PersonInviteLookupOutputDTO of(PersonInviteLookupStatus status, UUID personId, String name, String email) {
        return new PersonInviteLookupOutputDTO(status, personId, name, email);
    }
}

package com.vmarcante.time_tracker.core.domain.person.model.filter;

public final class PersonFilterMapper {

    private PersonFilterMapper() {
    }

    public static PersonFilter byEmail(String email) {
        PersonFilter filter = new PersonFilter();
        filter.setEmail(email);
        return filter;
    }
}

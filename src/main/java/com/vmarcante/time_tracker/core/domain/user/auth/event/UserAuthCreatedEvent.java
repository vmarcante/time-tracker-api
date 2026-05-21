package com.vmarcante.time_tracker.core.domain.user.auth.event;

import com.vmarcante.time_tracker.core.domain.person.model.Person;

import lombok.Data;

@Data
public class UserAuthCreatedEvent {

    private final Person person;
    private final String accessToken;
    private final String locale;

    public UserAuthCreatedEvent(Person person, String accessToken) {
        this.person = person;
        this.accessToken = accessToken;
        this.locale = person.getLocale() != null ? person.getLocale() : "pt";
    }
}

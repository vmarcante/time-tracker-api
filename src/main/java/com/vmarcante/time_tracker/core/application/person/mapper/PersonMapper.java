package com.vmarcante.time_tracker.core.application.person.mapper;

import com.vmarcante.time_tracker.core.application.person.dto.CreatePersonDTO;
import com.vmarcante.time_tracker.core.domain.person.model.Person;

public class PersonMapper {

    public static Person toModel(CreatePersonDTO input) {
        Person person = new Person();
        person.setName(input.name());
        person.setEmail(input.email());
        person.setPhone(input.phone());
        person.setLocale(input.locale() != null ? input.locale() : "pt");
        return person;
    }
}

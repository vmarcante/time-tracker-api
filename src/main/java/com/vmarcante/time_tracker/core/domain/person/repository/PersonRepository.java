package com.vmarcante.time_tracker.core.domain.person.repository;

import java.util.Optional;
import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.person.model.Person;

public interface PersonRepository {

    Person save(Person person);

    Person updateAuditFields(UUID personId, UUID createdBy, UUID updatedBy);

    boolean existsByEmail(String email);

    Optional<Person> findById(UUID id);
}

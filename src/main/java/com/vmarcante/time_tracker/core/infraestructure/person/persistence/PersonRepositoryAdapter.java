package com.vmarcante.time_tracker.core.infraestructure.person.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.base.infraestructure.persistence.converter.EncryptedFieldHashUtils;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.infraestructure.person.mapper.PersonPersistenceMapper;

@Component
public class PersonRepositoryAdapter implements PersonRepository {

    private final PersonJpaRepository jpaRepository;

    public PersonRepositoryAdapter(PersonJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Person save(Person person) {
        PersonJpaEntity entity = PersonPersistenceMapper.toEntity(person);
        PersonJpaEntity savedEntity = jpaRepository.save(entity);
        return PersonPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Person updateAuditFields(UUID personId, UUID createdBy, UUID updatedBy) {
        PersonJpaEntity entity = jpaRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Person not found with id: " + personId));
        entity.setCreatedBy(createdBy);
        entity.setUpdatedBy(updatedBy);
        PersonJpaEntity savedEntity = jpaRepository.save(entity);
        return PersonPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        String emailHash = EncryptedFieldHashUtils.generateSearchHash(email);
        return jpaRepository.existsByEmailHash(emailHash);
    }

    @Override
    public Optional<Person> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(PersonPersistenceMapper::toDomain);
    }
}

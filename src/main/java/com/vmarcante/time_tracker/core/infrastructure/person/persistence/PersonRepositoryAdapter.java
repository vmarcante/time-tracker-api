package com.vmarcante.time_tracker.core.infrastructure.person.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.model.filter.PersonFilter;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.infrastructure.person.mapper.PersonPersistenceMapper;

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
        PersonFilter filter = new PersonFilter();
        filter.setEmail(email);
        return jpaRepository.exists(PersonRepositorySpecification.withFilter(filter));
    }

    @Override
    public Optional<Person> findByEmail(String email) {
        PersonFilter filter = new PersonFilter();
        filter.setEmail(email);
        return jpaRepository.findOne(PersonRepositorySpecification.withFilter(filter))
                .map(PersonPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Person> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(PersonPersistenceMapper::toDomain);
    }

    @Override
    public List<Person> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids)
                .stream()
                .map(PersonPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<String> findNameById(UUID id) {
        return jpaRepository.findNameById(id);
    }

    @Override
    public Map<UUID, String> findNamesByIds(Collection<UUID> ids) {
        return jpaRepository.findIdAndNameByIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (String) row[1]));
    }

    @Override
    public void updateLocale(UUID personId, String locale) {
        jpaRepository.updateLocaleById(personId, locale);
    }
}

package com.vmarcante.time_tracker.core.infraestructure.person.mapper;

import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.infraestructure.person.persistence.PersonJpaEntity;
import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

public class PersonPersistenceMapper {

    public static PersonJpaEntity toEntity(Person person) {
        PersonJpaEntity entity = new PersonJpaEntity();
        entity.setId(person.getId());
        entity.setName(person.getName());
        entity.setEmail(person.getEmail().address());
        entity.setPhone(person.getPhone() != null ? person.getPhone().number() : null);
        entity.setAge(person.getAge());
        entity.setCreatedAt(person.getCreatedAt());
        entity.setUpdatedAt(person.getUpdatedAt());
        entity.setSeqId(person.getSeqId());
        entity.setCreatedBy(person.getCreatedBy());
        entity.setUpdatedBy(person.getUpdatedBy());
        entity.setLocale(person.getLocale() != null ? person.getLocale() : "pt");
        return entity;
    }

    public static Person toDomain(PersonJpaEntity entity) {
        Person person = new Person();
        person.setId(entity.getId());
        person.setName(entity.getName());
        person.setEmail(new Email(entity.getEmail()));
        person.setPhone(entity.getPhone() != null ? new Phone(entity.getPhone()) : null);
        person.setAge(entity.getAge());
        person.setCreatedAt(entity.getCreatedAt());
        person.setUpdatedAt(entity.getUpdatedAt());
        person.setSeqId(entity.getSeqId());
        person.setCreatedBy(entity.getCreatedBy());
        person.setUpdatedBy(entity.getUpdatedBy());
        person.setLocale(entity.getLocale() != null ? entity.getLocale() : "pt");
        return person;
    }
}

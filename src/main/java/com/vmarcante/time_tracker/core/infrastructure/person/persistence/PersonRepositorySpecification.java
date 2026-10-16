package com.vmarcante.time_tracker.core.infrastructure.person.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.vmarcante.time_tracker.base.infrastructure.persistence.converter.EncryptedFieldHashUtils;
import com.vmarcante.time_tracker.core.domain.person.model.filter.PersonFilter;

public final class PersonRepositorySpecification {

    private PersonRepositorySpecification() {
    }

    public static Specification<PersonJpaEntity> withFilter(PersonFilter filter) {
        if (filter == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(List.of(
                hasId(filter.getId()),
                hasName(filter.getName()),
                hasEmail(filter.getEmail()),
                hasPhone(filter.getPhone()),
                isActive(filter.getActive())));
    }

    public static Specification<PersonJpaEntity> hasId(UUID id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    public static Specification<PersonJpaEntity> hasName(String name) {
        return (root, query, cb) -> name == null || name.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("nameHash"), EncryptedFieldHashUtils.generateSearchHash(name));
    }

    public static Specification<PersonJpaEntity> hasEmail(String email) {
        return (root, query, cb) -> email == null || email.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("emailHash"), EncryptedFieldHashUtils.generateSearchHash(email));
    }

    public static Specification<PersonJpaEntity> hasPhone(String phone) {
        return (root, query, cb) -> phone == null || phone.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("phoneHash"), EncryptedFieldHashUtils.generateSearchHash(phone));
    }

    public static Specification<PersonJpaEntity> isActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }
}

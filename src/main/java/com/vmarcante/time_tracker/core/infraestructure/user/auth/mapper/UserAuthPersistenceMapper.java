package com.vmarcante.time_tracker.core.infraestructure.user.auth.mapper;

import com.vmarcante.time_tracker.core.domain.user.auth.model.UserAuth;
import com.vmarcante.time_tracker.core.domain.user.enums.AffiliationStatus;
import com.vmarcante.time_tracker.core.domain.user.enums.UserRoleType;
import com.vmarcante.time_tracker.core.infraestructure.person.mapper.PersonPersistenceMapper;
import com.vmarcante.time_tracker.core.infraestructure.user.auth.persistence.UserAuthJpaEntity;

public class UserAuthPersistenceMapper {

    public static UserAuthJpaEntity toEntity(UserAuth domain) {
        if (domain == null) {
            return null;
        }

        UserAuthJpaEntity entity = new UserAuthJpaEntity();
        entity.setId(domain.getId());
        entity.setSeqId(domain.getSeqId());
        entity.setUsername(domain.getUsername());
        entity.setPassword(domain.getPassword());
        entity.setPasswordSalt(domain.getPasswordSalt());
        entity.setAccessToken(domain.getAccessToken());
        entity.setActive(domain.getActive() != null ? domain.getActive() : false);
        entity.setUserConfirmed(domain.getUserConfirmed() != null ? domain.getUserConfirmed() : false);
        entity.setFailedAttempts(domain.getFailedAttempts());
        entity.setLastPasswordChange(domain.getLastPasswordChange());
        entity.setPasswordResetRequested(domain.getPasswordResetRequested());
        entity.setLastPasswordResetRequest(domain.getLastPasswordResetRequest());
        entity.setLastLogin(domain.getLastLogin());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setRole(domain.getRole() != null ? domain.getRole() : UserRoleType.USER);
        entity.setAffiliation(domain.getAffiliation() != null ? domain.getAffiliation() : AffiliationStatus.PENDING);

        return entity;
    }

    public static UserAuth toDomain(UserAuthJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        UserAuth domain = new UserAuth();
        domain.setId(entity.getId());
        domain.setSeqId(entity.getSeqId());
        domain.setUsername(entity.getUsername());
        domain.setPassword(entity.getPassword());
        domain.setPasswordSalt(entity.getPasswordSalt());
        domain.setAccessToken(entity.getAccessToken());
        domain.setActive(entity.getActive() != null ? entity.getActive() : false);
        domain.setUserConfirmed(entity.getUserConfirmed() != null ? entity.getUserConfirmed() : false);
        domain.setFailedAttempts(entity.getFailedAttempts());
        domain.setLastPasswordChange(entity.getLastPasswordChange());
        domain.setPasswordResetRequested(entity.getPasswordResetRequested());
        domain.setLastPasswordResetRequest(entity.getLastPasswordResetRequest());
        domain.setLastLogin(entity.getLastLogin());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setRole(entity.getRole() != null ? entity.getRole() : UserRoleType.USER);
        domain.setAffiliation(entity.getAffiliation() != null ? entity.getAffiliation() : AffiliationStatus.PENDING);

        if (entity.getPerson() != null) {
            domain.setPerson(PersonPersistenceMapper.toDomain(entity.getPerson()));
        }
        
        return domain;
    }
}

package com.vmarcante.time_tracker.core.infraestructure.user.session.mapper;

import com.vmarcante.time_tracker.core.domain.user.session.model.UserSession;
import com.vmarcante.time_tracker.core.infraestructure.user.session.persistence.UserSessionJpaEntity;

public final class UserSessionPersistenceMapper {

    private UserSessionPersistenceMapper() {
    }

    public static UserSessionJpaEntity toEntity(UserSession domain) {
        if (domain == null) {
            return null;
        }

        UserSessionJpaEntity entity = new UserSessionJpaEntity();
        entity.setId(domain.getId());
        entity.setSeqId(domain.getSeqId());
        entity.setUserId(domain.getUserId());
        entity.setRefreshToken(domain.getRefreshToken());
        entity.setRefreshTokenHash(domain.getRefreshTokenHash());
        entity.setRefreshTokenExpiry(domain.getRefreshTokenExpiry());
        entity.setDeviceInfo(domain.getDeviceInfo());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setActive(domain.getActive());
        entity.setLastActivityAt(domain.getLastActivityAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedBy(domain.getUpdatedBy());

        return entity;
    }

    public static UserSession toDomain(UserSessionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        UserSession domain = new UserSession();
        domain.setId(entity.getId());
        domain.setSeqId(entity.getSeqId());
        domain.setUserId(entity.getUserId());
        domain.setRefreshToken(entity.getRefreshToken());
        domain.setRefreshTokenHash(entity.getRefreshTokenHash());
        domain.setRefreshTokenExpiry(entity.getRefreshTokenExpiry());
        domain.setDeviceInfo(entity.getDeviceInfo());
        domain.setIpAddress(entity.getIpAddress());
        domain.setUserAgent(entity.getUserAgent());
        domain.setActive(entity.getActive());
        domain.setLastActivityAt(entity.getLastActivityAt());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setUpdatedBy(entity.getUpdatedBy());

        return domain;
    }
}

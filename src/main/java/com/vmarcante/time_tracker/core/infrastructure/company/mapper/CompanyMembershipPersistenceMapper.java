package com.vmarcante.time_tracker.core.infrastructure.company.mapper;

import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.infrastructure.company.persistence.CompanyMembershipJpaEntity;

public final class CompanyMembershipPersistenceMapper {

    private CompanyMembershipPersistenceMapper() {
    }

    public static CompanyMembership toDomain(CompanyMembershipJpaEntity entity) {
        CompanyMembership membership = new CompanyMembership();
        membership.setId(entity.getId());
        membership.setSeqId(entity.getSeqId());
        membership.setUserId(entity.getUserId());
        membership.setCompanyId(entity.getCompanyId());
        membership.setRole(entity.getRole());
        membership.setOrigin(entity.getOrigin());
        membership.setApproved(entity.getApproved());
        membership.setRequestReason(entity.getRequestReason());
        membership.setRejectionReason(entity.getRejectionReason());
        membership.setApprovedAt(entity.getApprovedAt());
        membership.setApprovedBy(entity.getApprovedBy());
        membership.setActive(entity.getActive());
        membership.setCreatedAt(entity.getCreatedAt());
        membership.setUpdatedAt(entity.getUpdatedAt());
        membership.setCreatedBy(entity.getCreatedBy());
        membership.setUpdatedBy(entity.getUpdatedBy());
        return membership;
    }

    public static CompanyMembershipJpaEntity toEntity(CompanyMembership membership) {
        CompanyMembershipJpaEntity entity = new CompanyMembershipJpaEntity();
        entity.setId(membership.getId());
        entity.setUserId(membership.getUserId());
        entity.setCompanyId(membership.getCompanyId());
        entity.setRole(membership.getRole());
        entity.setOrigin(membership.getOrigin());
        entity.setApproved(membership.getApproved());
        entity.setRequestReason(membership.getRequestReason());
        entity.setRejectionReason(membership.getRejectionReason());
        entity.setApprovedAt(membership.getApprovedAt());
        entity.setApprovedBy(membership.getApprovedBy());
        entity.setActive(membership.getActive());
        entity.setCreatedBy(membership.getCreatedBy());
        entity.setUpdatedBy(membership.getUpdatedBy());
        return entity;
    }
}

package com.vmarcante.time_tracker.core.infraestructure.company.mapper;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.infraestructure.company.persistence.CompanyJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.company.persistence.UserCompanyJpaEntity;

public class CompanyPersistenceMapper {

    public static Company toDomain(CompanyJpaEntity entity) {
        Company company = new Company();
        company.setId(entity.getId());
        company.setSeqId(entity.getSeqId());
        company.setLegalName(entity.getLegalName());
        company.setTradeName(entity.getTradeName());
        company.setDocument(entity.getDocument());
        company.setDescription(entity.getDescription());
        company.setTimezone(entity.getTimezone());
        company.setActive(entity.getActive());
        company.setCreatedAt(entity.getCreatedAt());
        company.setUpdatedAt(entity.getUpdatedAt());
        company.setCreatedBy(entity.getCreatedBy());
        company.setUpdatedBy(entity.getUpdatedBy());
        return company;
    }

    public static CompanyJpaEntity toEntity(Company company) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setId(company.getId());
        entity.setLegalName(company.getLegalName());
        entity.setTradeName(company.getTradeName());
        entity.setDocument(company.getDocument());
        entity.setDescription(company.getDescription());
        entity.setTimezone(company.getTimezone());
        entity.setActive(company.getActive());
        entity.setCreatedBy(company.getCreatedBy());
        entity.setUpdatedBy(company.getUpdatedBy());
        return entity;
    }

    public static CompanyMembership toDomain(UserCompanyJpaEntity entity) {
        CompanyMembership membership = new CompanyMembership();
        membership.setId(entity.getId());
        membership.setSeqId(entity.getSeqId());
        membership.setUserId(entity.getUserId());
        membership.setCompanyId(entity.getCompanyId());
        membership.setRole(entity.getRole());
        membership.setOrigin(entity.getOrigin());
        membership.setApproved(entity.getApproved());
        membership.setApprovedAt(entity.getApprovedAt());
        membership.setApprovedBy(entity.getApprovedBy());
        membership.setActive(entity.getActive());
        membership.setCreatedAt(entity.getCreatedAt());
        membership.setUpdatedAt(entity.getUpdatedAt());
        membership.setCreatedBy(entity.getCreatedBy());
        membership.setUpdatedBy(entity.getUpdatedBy());
        return membership;
    }

    public static UserCompanyJpaEntity toEntity(CompanyMembership membership) {
        UserCompanyJpaEntity entity = new UserCompanyJpaEntity();
        entity.setId(membership.getId());
        entity.setUserId(membership.getUserId());
        entity.setCompanyId(membership.getCompanyId());
        entity.setRole(membership.getRole());
        entity.setOrigin(membership.getOrigin());
        entity.setApproved(membership.getApproved());
        entity.setApprovedAt(membership.getApprovedAt());
        entity.setApprovedBy(membership.getApprovedBy());
        entity.setActive(membership.getActive());
        entity.setCreatedBy(membership.getCreatedBy());
        entity.setUpdatedBy(membership.getUpdatedBy());
        return entity;
    }
}

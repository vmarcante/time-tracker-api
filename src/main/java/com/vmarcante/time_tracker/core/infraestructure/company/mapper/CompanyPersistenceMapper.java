package com.vmarcante.time_tracker.core.infraestructure.company.mapper;

import com.vmarcante.time_tracker.core.domain.company.model.Company;
import com.vmarcante.time_tracker.core.infraestructure.company.persistence.CompanyJpaEntity;

public final class CompanyPersistenceMapper {

    private CompanyPersistenceMapper() {
    }

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
}

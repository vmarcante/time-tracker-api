package com.vmarcante.time_tracker.core.application.company.enums;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.base.application.pagination.SortType;

public enum CompanySortType implements SortType {
    TRADE_NAME_ASC(Sort.by(Sort.Direction.ASC, "tradeName")),
    TRADE_NAME_DESC(Sort.by(Sort.Direction.DESC, "tradeName")),
    CREATED_AT_ASC(Sort.by(Sort.Direction.ASC, "createdAt")),
    CREATED_AT_DESC(Sort.by(Sort.Direction.DESC, "createdAt"));

    private final Sort sort;

    CompanySortType(Sort sort) {
        this.sort = sort;
    }

    @Override
    public Sort generateSort() {
        return sort;
    }
}

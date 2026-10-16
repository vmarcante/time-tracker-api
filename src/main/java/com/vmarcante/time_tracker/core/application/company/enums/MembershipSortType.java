package com.vmarcante.time_tracker.core.application.company.enums;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.shared.interfaces.SortType;

public enum MembershipSortType implements SortType {
    CREATED_AT_ASC(Sort.by(Sort.Direction.ASC, "createdAt")),
    CREATED_AT_DESC(Sort.by(Sort.Direction.DESC, "createdAt"));

    private final Sort sort;

    MembershipSortType(Sort sort) {
        this.sort = sort;
    }

    @Override
    public Sort generateSort() {
        return sort;
    }
}

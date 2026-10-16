package com.vmarcante.time_tracker.core.application.team.enums;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.base.application.pagination.SortType;

public enum TeamSortType implements SortType {
    NAME_ASC(Sort.by(Sort.Direction.ASC, "name")),
    NAME_DESC(Sort.by(Sort.Direction.DESC, "name")),
    CREATED_AT_ASC(Sort.by(Sort.Direction.ASC, "createdAt")),
    CREATED_AT_DESC(Sort.by(Sort.Direction.DESC, "createdAt"));

    private final Sort sort;

    TeamSortType(Sort sort) {
        this.sort = sort;
    }

    @Override
    public Sort generateSort() {
        return sort;
    }
}

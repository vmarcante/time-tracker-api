package com.vmarcante.time_tracker.core.application.timeentry.entry.enums;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.base.application.pagination.SortType;

public enum TimeEntrySortType implements SortType {
    NAME_ASC(Sort.Direction.ASC, "name"),
    NAME_DESC(Sort.Direction.DESC, "name"),
    CREATED_AT_ASC(Sort.Direction.ASC, "createdAt"),
    CREATED_AT_DESC(Sort.Direction.DESC, "createdAt");

    private final Sort sort;

    TimeEntrySortType(Sort.Direction direction, String field) {
        this.sort = Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "seqId"));
    }

    @Override
    public Sort generateSort() {
        return sort;
    }
}

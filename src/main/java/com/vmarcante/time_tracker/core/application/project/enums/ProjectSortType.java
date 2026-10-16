package com.vmarcante.time_tracker.core.application.project.enums;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.shared.interfaces.SortType;

public enum ProjectSortType implements SortType {
    NAME_ASC(Sort.by(Sort.Direction.ASC, "name")),
    NAME_DESC(Sort.by(Sort.Direction.DESC, "name")),
    CREATED_AT_ASC(Sort.by(Sort.Direction.ASC, "createdAt")),
    CREATED_AT_DESC(Sort.by(Sort.Direction.DESC, "createdAt"));

    private final Sort sort;

    ProjectSortType(Sort sort) {
        this.sort = sort;
    }

    @Override
    public Sort generateSort() {
        return sort;
    }
}

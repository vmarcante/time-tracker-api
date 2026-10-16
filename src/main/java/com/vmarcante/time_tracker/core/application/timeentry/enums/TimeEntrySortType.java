package com.vmarcante.time_tracker.core.application.timeentry.enums;

import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.shared.interfaces.SortType;

public enum TimeEntrySortType implements SortType {
    RECENT_FIRST;

    @Override
    public Sort generateSort() {
        return null;
    }
}

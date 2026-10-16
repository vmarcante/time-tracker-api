package com.vmarcante.time_tracker.base.application.pagination;

import org.springframework.data.domain.Sort;

public interface SortType {
    Sort generateSort();
}

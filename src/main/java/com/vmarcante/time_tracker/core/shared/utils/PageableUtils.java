package com.vmarcante.time_tracker.core.shared.utils;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vmarcante.time_tracker.core.shared.interfaces.SortType;

public class PageableUtils {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "seqId";

    private PageableUtils() {
    }

    public static Pageable pageable(Integer pageNumber, Integer pageSize, SortType sort) {
        return pageable(pageNumber, pageSize, sort, DEFAULT_SORT_FIELD);
    }

    public static Pageable pageable(Integer pageNumber, Integer pageSize, SortType sort, String customDefaultField) {
        List<SortType> sorts = sort != null ? List.of(sort) : List.of();
        return multiSortPageable(pageNumber, pageSize, sorts, customDefaultField);
    }

    public static Pageable multiSortPageable(Integer pageNumber, Integer pageSize, List<SortType> sorts,
            String customDefaultField) {
        customDefaultField = customDefaultField != null ? customDefaultField : DEFAULT_SORT_FIELD;
        Sort defaultSort = Sort.by(Sort.Direction.ASC, customDefaultField);
        Sort combinedSort = null;

        if (sorts != null && !sorts.isEmpty()) {
            combinedSort = sorts.stream()
                    .map(SortType::generateSort)
                    .filter(Objects::nonNull)
                    .reduce(Sort::and)
                    .orElse(null);
        }

        Sort effectiveSort = combinedSort != null ? combinedSort : defaultSort;
        int effectivePage = normalizePageNumber(pageNumber);
        int effectiveSize = normalizePageSize(pageSize);

        return PageRequest.of(effectivePage, effectiveSize, effectiveSort);
    }

    public static Pageable unpageable(SortType sort) {
        return unpageable(sort, DEFAULT_SORT_FIELD);
    }

    public static Pageable unpageable(SortType sort, String customDefaultField) {
        List<SortType> sorts = sort != null ? List.of(sort) : List.of();
        return multiSortUnpageable(sorts, customDefaultField);
    }

    public static Pageable multiSortUnpageable(List<SortType> sorts, String customDefaultField) {
        customDefaultField = customDefaultField != null ? customDefaultField : DEFAULT_SORT_FIELD;
        Sort defaultSort = Sort.by(Sort.Direction.ASC, customDefaultField);
        Sort combinedSort = null;

        if (sorts != null && !sorts.isEmpty()) {
            combinedSort = sorts.stream()
                    .map(SortType::generateSort)
                    .filter(Objects::nonNull)
                    .reduce(Sort::and)
                    .orElse(null);
        }

        Sort effectiveSort = combinedSort != null ? combinedSort : defaultSort;
        return Pageable.unpaged(effectiveSort);
    }

    private static int normalizePageNumber(Integer pageNumber) {
        if (pageNumber == null || pageNumber < 1) {
            return 0;
        }
        return pageNumber - 1; // Converts 1-based to 0-based
    }

    private static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}

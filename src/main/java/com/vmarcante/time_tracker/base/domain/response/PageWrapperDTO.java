package com.vmarcante.time_tracker.base.domain.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageWrapperDTO<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty) {

    public static <T> PageWrapperDTO<T> of(Page<T> page) {
        return new PageWrapperDTO<>(
                page.getContent(),
                page.getNumber() + 1, // Convert 0-based to 1-based
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty());
    }
}

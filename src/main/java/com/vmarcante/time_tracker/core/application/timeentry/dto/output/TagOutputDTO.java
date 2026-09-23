package com.vmarcante.time_tracker.core.application.timeentry.dto.output;

import java.util.UUID;

import com.vmarcante.time_tracker.core.domain.timeentry.model.Tag;

public record TagOutputDTO(
        UUID id,
        String name,
        String color) {

    public static TagOutputDTO from(Tag tag) {
        return new TagOutputDTO(tag.getId(), tag.getName(), tag.getColor());
    }
}

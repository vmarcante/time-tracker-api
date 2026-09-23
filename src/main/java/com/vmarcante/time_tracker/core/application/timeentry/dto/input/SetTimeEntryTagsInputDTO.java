package com.vmarcante.time_tracker.core.application.timeentry.dto.input;

import java.util.List;
import java.util.UUID;

public record SetTimeEntryTagsInputDTO(
        List<UUID> tagIds) {
}

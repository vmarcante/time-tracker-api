package com.vmarcante.time_tracker.core.application.project.link.dto.input;

import java.util.List;
import java.util.UUID;

public record ProjectTeamMembersInputDTO(
        UUID teamId,
        List<UUID> memberIds) {
}

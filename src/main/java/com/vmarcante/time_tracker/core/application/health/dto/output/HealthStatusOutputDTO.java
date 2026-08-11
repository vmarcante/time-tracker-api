package com.vmarcante.time_tracker.core.application.health.dto.output;

import java.util.List;
import java.util.Map;

public record HealthStatusOutputDTO(
        String status,
        List<HealthComponentOutputDTO> components) {

    public record HealthComponentOutputDTO(
            String name,
            String status,
            Map<String, Object> details,
            boolean critical) {
    }
}

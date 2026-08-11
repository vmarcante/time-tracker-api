package com.vmarcante.time_tracker.core.domain.health.model;

import java.util.Map;

public record HealthComponent(
    String name,
    String status,
    Map<String, Object> details,
    boolean critical) {
}

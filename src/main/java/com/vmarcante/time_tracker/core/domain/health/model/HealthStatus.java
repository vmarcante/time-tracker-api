package com.vmarcante.time_tracker.core.domain.health.model;

import java.util.List;

public record HealthStatus(String status, List<HealthComponent> components) {
}

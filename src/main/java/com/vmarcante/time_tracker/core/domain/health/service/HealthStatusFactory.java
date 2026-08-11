package com.vmarcante.time_tracker.core.domain.health.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;
import com.vmarcante.time_tracker.core.domain.health.model.HealthStatus;
import com.vmarcante.time_tracker.core.domain.health.port.HealthStatusProvider;

@Service
public class HealthStatusFactory {

    private final List<HealthStatusProvider> providers;

    public HealthStatusFactory(List<HealthStatusProvider> providers) {
        this.providers = providers;
    }

    public HealthStatus buildStatus() {
        List<HealthComponent> components = providers.stream()
                .map(provider -> provider.getComponent())
                .toList();

        boolean allUp = components.stream()
                .allMatch(component -> "UP".equals(component.status()));

        if (allUp) {
            return new HealthStatus("UP", components);
        }

        boolean anyCriticalDown = components.stream()
                .filter(component -> component.critical())
                .anyMatch(component -> "DOWN".equals(component.status()));

        return new HealthStatus(anyCriticalDown ? "DOWN" : "PARTIAL", components);
    }
}

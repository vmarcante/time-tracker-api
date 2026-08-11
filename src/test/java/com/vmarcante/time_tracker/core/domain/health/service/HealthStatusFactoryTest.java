package com.vmarcante.time_tracker.core.domain.health.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;
import com.vmarcante.time_tracker.core.domain.health.model.HealthStatus;
import com.vmarcante.time_tracker.core.domain.health.port.HealthStatusProvider;

class HealthStatusFactoryTest {

    @Test
    void shouldReturnUpWhenAllComponentsAreUp() {
        HealthStatusFactory factory = new HealthStatusFactory(List.of(
                upProvider("database"),
                upProvider("jvm")));

        HealthStatus status = factory.buildStatus();

        assertEquals("UP", status.status());
        assertEquals(2, status.components().size());
    }

    @Test
    void shouldReturnDownWhenCriticalComponentIsDown() {
        HealthStatusFactory factory = new HealthStatusFactory(List.of(
                downProvider("database", true),
                upProvider("jvm")));

        HealthStatus status = factory.buildStatus();

        assertEquals("DOWN", status.status());
    }

    @Test
    void shouldReturnPartialWhenNonCriticalComponentIsDown() {
        HealthStatusFactory factory = new HealthStatusFactory(List.of(
                upProvider("database"),
                downProvider("redis", false)));

        HealthStatus status = factory.buildStatus();

        assertEquals("PARTIAL", status.status());
    }

    private HealthStatusProvider upProvider(String name) {
        return () -> new HealthComponent(name, "UP", Map.of(), true);
    }

    private HealthStatusProvider downProvider(String name, boolean critical) {
        return () -> new HealthComponent(name, "DOWN", Collections.emptyMap(), critical);
    }
}

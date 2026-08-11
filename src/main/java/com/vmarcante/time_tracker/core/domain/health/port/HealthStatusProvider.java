package com.vmarcante.time_tracker.core.domain.health.port;

import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;

public interface HealthStatusProvider {

    HealthComponent getComponent();
}

package com.vmarcante.time_tracker.core.application.health.in;

import com.vmarcante.time_tracker.core.application.health.dto.output.HealthStatusOutputDTO;

public interface HealthCheckUseCase {

    HealthStatusOutputDTO execute();
}

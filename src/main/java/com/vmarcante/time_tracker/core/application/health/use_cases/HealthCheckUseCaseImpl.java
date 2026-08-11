package com.vmarcante.time_tracker.core.application.health.use_cases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.core.application.health.dto.output.HealthStatusOutputDTO;
import com.vmarcante.time_tracker.core.application.health.dto.output.HealthStatusOutputDTO.HealthComponentOutputDTO;
import com.vmarcante.time_tracker.core.application.health.in.HealthCheckUseCase;
import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;
import com.vmarcante.time_tracker.core.domain.health.model.HealthStatus;
import com.vmarcante.time_tracker.core.domain.health.service.HealthStatusFactory;

@Service
public class HealthCheckUseCaseImpl implements HealthCheckUseCase {

    private final HealthStatusFactory healthStatusFactory;

    public HealthCheckUseCaseImpl(HealthStatusFactory healthStatusFactory) {
        this.healthStatusFactory = healthStatusFactory;
    }

    @Override
    public HealthStatusOutputDTO execute() {
        HealthStatus status = healthStatusFactory.buildStatus();
        List<HealthComponentOutputDTO> components = status.components().stream()
                .map(component -> toComponentDTO(component))
                .toList();
        return new HealthStatusOutputDTO(status.status(), components);
    }

    private HealthComponentOutputDTO toComponentDTO(HealthComponent component) {
        return new HealthComponentOutputDTO(
                component.name(),
                component.status(),
                component.details(),
                component.critical());
    }
}

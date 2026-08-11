package com.vmarcante.time_tracker.core.infraestructure.health.adapters;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;
import com.vmarcante.time_tracker.core.domain.health.port.HealthStatusProvider;

@Component
public class JvmHealthAdapter implements HealthStatusProvider {

    @Override
    public HealthComponent getComponent() {
        Map<String, Object> details = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        details.put("total", runtime.totalMemory());
        details.put("free", runtime.freeMemory());
        details.put("max", runtime.maxMemory());
        details.put("used", runtime.totalMemory() - runtime.freeMemory());
        details.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        return new HealthComponent("jvm", "UP", details, true);
    }
}

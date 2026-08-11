package com.vmarcante.time_tracker.core.infraestructure.health.adapters;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;
import com.vmarcante.time_tracker.core.domain.health.port.HealthStatusProvider;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

@Component
public class DatabaseHealthAdapter implements HealthStatusProvider {

    private final DataSource dataSource;

    public DatabaseHealthAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public HealthComponent getComponent() {
        boolean healthy = checkDatabase();
        Map<String, Object> details = new HashMap<>();
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
            details.put("activeConnections", poolBean.getActiveConnections());
            details.put("idleConnections", poolBean.getIdleConnections());
            details.put("totalConnections", poolBean.getTotalConnections());
            details.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
            details.put("maxConnections", hikariDataSource.getMaximumPoolSize());
        }
        return new HealthComponent("database", healthy ? "UP" : "DOWN", details, true);
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
}

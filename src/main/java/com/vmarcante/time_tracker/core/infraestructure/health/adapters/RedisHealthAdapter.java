package com.vmarcante.time_tracker.core.infraestructure.health.adapters;

import java.util.HashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.health.model.HealthComponent;
import com.vmarcante.time_tracker.core.domain.health.port.HealthStatusProvider;

@Component
public class RedisHealthAdapter implements HealthStatusProvider {

    private final ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider;

    public RedisHealthAdapter(ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider) {
        this.redisConnectionFactoryProvider = redisConnectionFactoryProvider;
    }

    @Override
    public HealthComponent getComponent() {
        RedisConnectionFactory factory = redisConnectionFactoryProvider.getIfAvailable();
        if (factory == null) {
            return new HealthComponent("redis", "UNKNOWN", new HashMap<>(), false);
        }
        boolean healthy = checkRedis(factory);
        return new HealthComponent("redis", healthy ? "UP" : "DOWN", new HashMap<>(), false);
    }

    private boolean checkRedis(RedisConnectionFactory factory) {
        try (RedisConnection connection = factory.getConnection()) {
            return "PONG".equals(connection.ping());
        } catch (Exception e) {
            return false;
        }
    }
}

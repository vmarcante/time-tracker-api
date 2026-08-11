package com.vmarcante.time_tracker.base.infraestructure.configurations;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class SchedulerLockConfiguration {

    @Bean
    public LockProvider lockProvider(Optional<RedisConnectionFactory> redisConnectionFactory, Environment environment) {
        if (redisConnectionFactory.isPresent()) {
            return new RedisLockProvider(redisConnectionFactory.get());
        }

        if (isProd(environment)) {
            throw new IllegalStateException(
                    "Redis is required in production for distributed locking. Set redis.enabled=true and configure Redis.");
        }

        return new NoOpLockProvider();
    }

    private boolean isProd(Environment environment) {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    private static class NoOpLockProvider implements LockProvider {
        @Override
        public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
            return Optional.of(() -> {
            });
        }
    }
}

package com.vmarcante.time_tracker.base.domain.event;

public interface DomainEventPublisher {

    void publish(Object event);
}

package com.vmarcante.time_tracker.base.infraestructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.base.domain.event.DomainEventPublisher;

@Component
public class SpringEventPublisherAdapter implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}

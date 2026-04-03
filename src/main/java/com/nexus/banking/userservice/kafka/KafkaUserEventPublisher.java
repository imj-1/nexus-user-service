package com.nexus.banking.userservice.kafka;

import com.nexus.banking.userservice.outbox.OutboxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaUserEventPublisher implements UserEventPublisherPort {

    @Override
    public void publish(OutboxEvent event) {
        // Temporary stub (real Kafka comes next)
        log.info("Publishing event: type={}, aggregateId={}", event.getType(), event.getAggregateId());
    }
}
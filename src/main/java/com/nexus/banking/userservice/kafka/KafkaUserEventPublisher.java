package com.nexus.banking.userservice.kafka;

import com.nexus.banking.userservice.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserEventPublisher implements UserEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Override
    public void publish(OutboxEvent event) {

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send(topic, event.getAggregateId(), event.getPayload());
            return true;
        });
    }
}
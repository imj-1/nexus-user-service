package com.nexus.banking.userservice.kafka;

import com.nexus.banking.userservice.outbox.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserEventPublisher implements UserEventPublisherPort {

    private static final String TOPIC = "user.events";
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(OutboxEvent event) {

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send(
                    TOPIC, event.getAggregateId(), // key
                    event.getPayload()      // value
                           );
            return true;
        });
    }
}
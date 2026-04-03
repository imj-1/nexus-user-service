package com.nexus.banking.userservice.outbox;

import com.nexus.banking.userservice.kafka.UserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository repository;
    private final UserEventPublisherPort publisher;

    @Transactional
    public void processOutboxEvents() {

        List<OutboxEvent> events = repository.findTop10ByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                publisher.publish(event);

                event.setProcessed(true);
                repository.save(event);

            } catch (Exception ex) {
                // stop processing batch on failure (retry later)
                break;
            }
        }
    }
}
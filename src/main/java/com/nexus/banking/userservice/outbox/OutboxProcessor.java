package com.nexus.banking.userservice.outbox;

import com.nexus.banking.userservice.kafka.UserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository repository;
    private final UserEventPublisherPort publisher;

    @Scheduled(fixedDelayString = "${outbox.poller.delay:5000}")
    @Transactional
    public void processOutboxEvents() {

        List<OutboxEvent> events = repository.findTop10ByProcessedFalseOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                publisher.publish(event);

                event.setProcessed(true);
                repository.save(event);

            } catch (Exception ex) {
                log.error("Failed to publish event id={}", event.getId(), ex);
                break;
            }
        }
    }
}
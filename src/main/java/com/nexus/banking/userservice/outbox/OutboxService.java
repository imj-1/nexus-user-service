package com.nexus.banking.userservice.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository repository;

    public OutboxEvent saveEvent(OutboxEvent event) {
        event.setCreatedAt(Instant.now());
        event.setProcessed(false);
        return repository.save(event);
    }
}
package com.nexus.banking.userservice.kafka;

import com.nexus.banking.userservice.outbox.OutboxEvent;

public interface UserEventPublisherPort {

    void publish(OutboxEvent event);
}
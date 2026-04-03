package com.nexus.banking.userservice.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findTop10ByProcessedFalseOrderByCreatedAtAsc();
}
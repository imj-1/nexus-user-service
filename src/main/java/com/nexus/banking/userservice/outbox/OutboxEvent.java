package com.nexus.banking.userservice.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String aggregateType;
    private String aggregateId;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;

    private boolean processed;
}
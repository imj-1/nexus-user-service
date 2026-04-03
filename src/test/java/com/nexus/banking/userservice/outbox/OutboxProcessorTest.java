package com.nexus.banking.userservice.outbox;

import com.nexus.banking.userservice.kafka.UserEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class OutboxProcessorTest {

    private OutboxEventRepository repository;
    private UserEventPublisherPort publisher;
    private OutboxProcessor processor;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventRepository.class);
        publisher = mock(UserEventPublisherPort.class);
        processor = new OutboxProcessor(repository, publisher);
    }

    @Test
    void shouldPublishAndMarkEventAsProcessed() {
        // given
        OutboxEvent event = OutboxEvent.builder()
                                       .id("1")
                                       .aggregateId("123")
                                       .type("user.registered")
                                       .payload("{\"userId\":\"123\"}")
                                       .processed(false)
                                       .build();

        when(repository.findTop10ByProcessedFalseOrderByCreatedAtAsc()).thenReturn(List.of(event));

        // when
        processor.processOutboxEvents();

        // then
        verify(publisher).publish(event);
        verify(repository).save(argThat(e -> e.isProcessed()));
    }

    @Test
    void shouldStopProcessingOnFailure() {
        OutboxEvent event1 = OutboxEvent.builder()
                                        .id("1")
                                        .processed(false)
                                        .build();
        OutboxEvent event2 = OutboxEvent.builder()
                                        .id("2")
                                        .processed(false)
                                        .build();

        when(repository.findTop10ByProcessedFalseOrderByCreatedAtAsc()).thenReturn(List.of(event1, event2));

        doThrow(new RuntimeException()).when(publisher)
                                       .publish(event1);

        processor.processOutboxEvents();

        verify(publisher).publish(event1);
        verify(publisher, never()).publish(event2);
    }
}
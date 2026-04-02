package com.nexus.banking.userservice.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxServiceTest {

    private OutboxEventRepository repository;
    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(OutboxEventRepository.class);
        outboxService = new OutboxService(repository);
    }

    @Test
    void shouldSaveOutboxEvent() {
        // given
        OutboxEvent event = OutboxEvent.builder()
                                       .aggregateType("USER")
                                       .aggregateId("123")
                                       .type("user.registered")
                                       .payload("{\"email\":\"test@nexus.com\"}")
                                       .build();

        Mockito.when(repository.save(Mockito.any()))
               .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        OutboxEvent saved = outboxService.saveEvent(event);

        // then
        assertThat(saved.getAggregateType()).isEqualTo("USER");
        assertThat(saved.getType()).isEqualTo("user.registered");
    }
}
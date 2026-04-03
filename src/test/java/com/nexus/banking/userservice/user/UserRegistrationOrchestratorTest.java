package com.nexus.banking.userservice.user;

import com.nexus.banking.userservice.keycloak.KeycloakUserProvisioningPort;
import com.nexus.banking.userservice.outbox.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UserRegistrationOrchestratorTest {

    private UserService userService;
    private KeycloakUserProvisioningPort keycloakPort;
    private OutboxService outboxService;
    private UserRegistrationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        keycloakPort = mock(KeycloakUserProvisioningPort.class);
        outboxService = mock(OutboxService.class);

        orchestrator = new UserRegistrationOrchestrator(userService, keycloakPort, outboxService);
    }

    @Test
    void shouldRollbackKeycloakUser_ifDatabaseSaveFails() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("fail@nexus.com", "Fail", "Case");

        when(keycloakPort.createUser(request)).thenReturn("kc-123");
        when(userService.registerUser(any(), eq("kc-123"))).thenThrow(new RuntimeException("DB failure"));

        // when
        try {
            orchestrator.registerUser(request);
        } catch (Exception ignored) {
        }

        // then
        verify(keycloakPort).deleteUser("kc-123");
    }

    @Test
    void shouldPublishUserRegisteredEvent_afterSuccessfulRegistration() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("test@nexus.com", "Test", "User");

        User savedUser = User.builder()
                             .id("123")
                             .email(request.email())
                             .build();

        when(keycloakPort.createUser(request)).thenReturn("kc-123");
        when(userService.registerUser(request, "kc-123")).thenReturn(savedUser);

        // when
        orchestrator.registerUser(request);

        // then
        verify(outboxService).saveEvent(argThat(event -> event.getAggregateType()
                                                              .equals("USER") && event.getAggregateId()
                                                                                      .equals("123") && event.getType()
                                                                                                             .equals("user.registered") && event.getPayload()
                                                                                                                                                .contains(
                                                                                                                                                        "123")));
    }
}
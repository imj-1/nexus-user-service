package com.nexus.banking.userservice.user;

import com.nexus.banking.userservice.keycloak.KeycloakUserProvisioningPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UserRegistrationOrchestratorTest {

    private UserService userService;
    private KeycloakUserProvisioningPort keycloakPort;
    private UserRegistrationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        keycloakPort = mock(KeycloakUserProvisioningPort.class);
        orchestrator = new UserRegistrationOrchestrator(userService, keycloakPort);
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
}
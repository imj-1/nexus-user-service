package com.nexus.banking.userservice.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.banking.userservice.keycloak.KeycloakUserProvisioningPort;
import com.nexus.banking.userservice.outbox.OutboxEvent;
import com.nexus.banking.userservice.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrationOrchestrator {

    private final UserService userService;
    private final KeycloakUserProvisioningPort keycloakPort;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public User registerUser(RegisterUserRequest request) {

        String keycloakId = keycloakPort.createUser(request);

        try {
            User user = userService.registerUser(request, keycloakId);

            String payload = toPayload(keycloakId);

            OutboxEvent event = OutboxEvent.builder()
                                           .aggregateType("USER")
                                           .aggregateId(user.getId())
                                           .type("user.registered")
                                           .payload(payload)
                                           .build();

            outboxService.saveEvent(event);

            return user;

        } catch (Exception ex) {
            keycloakPort.deleteUser(keycloakId);
            throw ex;
        }
    }

    private String toPayload(String userId) {
        try {
            return objectMapper.writeValueAsString(new UserRegisteredEvent(userId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    record UserRegisteredEvent(String userId) {}
}
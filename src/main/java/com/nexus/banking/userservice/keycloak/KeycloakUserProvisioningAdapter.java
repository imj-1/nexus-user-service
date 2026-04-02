package com.nexus.banking.userservice.keycloak;

import com.nexus.banking.userservice.user.RegisterUserRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KeycloakUserProvisioningAdapter implements KeycloakUserProvisioningPort {

    @Override
    public String createUser(RegisterUserRequest request) {
        // Temporary stub (will replace with real Keycloak API call)
        return UUID.randomUUID()
                   .toString();
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        // stub (real implementation later)
    }
}
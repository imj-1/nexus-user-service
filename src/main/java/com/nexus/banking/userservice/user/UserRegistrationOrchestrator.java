package com.nexus.banking.userservice.user;

import com.nexus.banking.userservice.keycloak.KeycloakUserProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationOrchestrator {

    private final UserService userService;
    private final KeycloakUserProvisioningPort keycloakPort;

    public User registerUser(RegisterUserRequest request) {

        String keycloakId = keycloakPort.createUser(request);

        try {
            return userService.registerUser(request, keycloakId);
        } catch (Exception ex) {
            keycloakPort.deleteUser(keycloakId);
            throw ex;
        }
    }
}
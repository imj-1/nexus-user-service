package com.nexus.banking.userservice.keycloak;

import com.nexus.banking.userservice.user.RegisterUserRequest;

public interface KeycloakUserProvisioningPort {

    String createUser(RegisterUserRequest request);
}
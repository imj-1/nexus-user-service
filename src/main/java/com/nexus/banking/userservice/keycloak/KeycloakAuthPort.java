package com.nexus.banking.userservice.keycloak;

import com.nexus.banking.userservice.api.LoginRequest;
import com.nexus.banking.userservice.api.LoginResponse;

public interface KeycloakAuthPort {
    LoginResponse login(LoginRequest request);
}
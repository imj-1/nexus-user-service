package com.nexus.banking.userservice.api;

import com.nexus.banking.userservice.keycloak.KeycloakAuthPort;
import com.nexus.banking.userservice.user.RegisterUserRequest;
import com.nexus.banking.userservice.user.User;
import com.nexus.banking.userservice.user.UserRegistrationOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationOrchestrator orchestrator;
    private final KeycloakAuthPort keycloakAuthPort;

    @PostMapping("/api/v1/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse publicRegister(@Valid @RequestBody RegisterUserRequest request) {
        User user = orchestrator.registerUser(request);
        return new UserResponse(user.getId(), user.getEmail());
    }

    @PostMapping("/api/v1/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return keycloakAuthPort.login(request);
    }
}
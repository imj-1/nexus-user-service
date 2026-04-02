package com.nexus.banking.userservice.user;

import com.nexus.banking.userservice.keycloak.KeycloakUserProvisioningPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakUserProvisioningPort keycloakPort;

    public User registerUser(RegisterUserRequest request) {

        String keycloakId = keycloakPort.createUser(request);

        User user = User.builder()
                        .email(request.email())
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .locked(false)
                        .keycloakId(keycloakId)
                        .build();

        return userRepository.save(user);
    }
}
package com.nexus.banking.userservice.user;

import com.nexus.banking.userservice.keycloak.KeycloakUserProvisioningPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    private UserRepository userRepository;
    private KeycloakUserProvisioningPort keycloakPort;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        keycloakPort = Mockito.mock(KeycloakUserProvisioningPort.class);
        userService = new UserService(userRepository, keycloakPort);
    }


    @Test
    void shouldRegisterUser_inDatabase() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("test@nexus.com", "Test", "User");

        String keycloakId = "kc-123";

        Mockito.when(userRepository.save(Mockito.any(User.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.registerUser(request, keycloakId);

        // then
        assertThat(result.getEmail()).isEqualTo("test@nexus.com");
        assertThat(result.getKeycloakId()).isEqualTo("kc-123");
        assertThat(result.isLocked()).isFalse();
    }
}
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
    void shouldRegisterUser() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("test@nexus.com", "Test", "User");

        User savedUser = User.builder()
                             .id("123")
                             .email(request.email())
                             .firstName(request.firstName())
                             .lastName(request.lastName())
                             .locked(false)
                             .build();

        Mockito.when(userRepository.save(Mockito.any(User.class)))
               .thenReturn(savedUser);

        // when
        User result = userService.registerUser(request);

        // then
        assertThat(result.getEmail()).isEqualTo("test@nexus.com");
        assertThat(result.isLocked()).isFalse();
    }
    
    @Test
    void shouldRegisterUser_andProvisionInKeycloak() {
        // given
        RegisterUserRequest request = new RegisterUserRequest("test@nexus.com", "Test", "User");

        Mockito.when(keycloakPort.createUser(request))
               .thenReturn("kc-123");

        Mockito.when(userRepository.save(Mockito.any(User.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.registerUser(request);

        // then
        assertThat(result.getKeycloakId()).isEqualTo("kc-123");
        assertThat(result.getEmail()).isEqualTo("test@nexus.com");
    }
}
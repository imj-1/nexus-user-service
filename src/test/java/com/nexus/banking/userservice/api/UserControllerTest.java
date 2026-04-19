package com.nexus.banking.userservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.banking.userservice.keycloak.KeycloakAuthPort;
import com.nexus.banking.userservice.user.RegisterUserRequest;
import com.nexus.banking.userservice.user.User;
import com.nexus.banking.userservice.user.UserRegistrationOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegistrationOrchestrator orchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KeycloakAuthPort keycloakAuthPort;

    @Test
    void shouldRegisterUser() throws Exception {

        RegisterUserRequest request = new RegisterUserRequest("test@nexus.com", "Test", "User", "password123");

        User user = User.builder()
                        .id("123")
                        .email("test@nexus.com")
                        .build();

        when(orchestrator.registerUser(request)).thenReturn(user);

        mockMvc.perform(post("/api/v1/users/register").contentType("application/json")
                                                      .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value("123"))
               .andExpect(jsonPath("$.email").value("test@nexus.com"));
    }
}
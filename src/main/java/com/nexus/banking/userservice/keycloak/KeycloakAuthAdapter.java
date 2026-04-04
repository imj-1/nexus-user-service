package com.nexus.banking.userservice.keycloak;

import com.nexus.banking.userservice.api.LoginRequest;
import com.nexus.banking.userservice.api.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KeycloakAuthAdapter implements KeycloakAuthPort {

    private final RestClient restClient;

    @Value("${keycloak.admin.base-url}")
    private String baseUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Override
    public LoginResponse login(LoginRequest request) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", request.email());
        form.add("password", request.password());

        return restClient.post()
                         .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                         .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                         .body(form)
                         .retrieve()
                         .body(LoginResponse.class);
    }
}
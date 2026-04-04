package com.nexus.banking.userservice.keycloak;

import com.nexus.banking.userservice.user.RegisterUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakUserProvisioningAdapter implements KeycloakUserProvisioningPort {

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
    public String createUser(RegisterUserRequest request) {
        String adminToken = getAdminToken();

        Map<String, Object> user = Map.of(
                "username",
                request.email(),   // ← add this
                "email",
                request.email(),
                "firstName",
                request.firstName(),
                "lastName",
                request.lastName(),
                "enabled",
                true,
                "emailVerified",
                false,
                "credentials",
                List.of(Map.of("type", "password", "value", request.password(), "temporary", false))
                                         );

        ResponseEntity<Void> response = restClient.post()
                                                  .uri(baseUrl + "/admin/realms/" + realm + "/users")
                                                  .header("Authorization", "Bearer " + adminToken)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .body(user)
                                                  .retrieve()
                                                  .toBodilessEntity();

        // Keycloak returns the new user's ID in the Location header
        String location = response.getHeaders()
                                  .getFirst("Location");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        String adminToken = getAdminToken();

        restClient.delete()
                  .uri(baseUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId)
                  .header("Authorization", "Bearer " + adminToken)
                  .retrieve()
                  .toBodilessEntity();
    }

    private String getAdminToken() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map response = restClient.post()
                                 .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                                 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                 .body(form)
                                 .retrieve()
                                 .body(Map.class);

        return (String) response.get("access_token");
    }
}
package kz.bitlab.keycloak_service.Keycloak_Service.service;

import kz.bitlab.keycloak_service.Keycloak_Service.dto.TokenResponseDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.UserCreateDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.UserSignInDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final Keycloak keycloak;
    private final RestTemplate restTemplate;

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public TokenResponseDto signIn(UserSignInDto userSignInDto) {
        String tokenEndPoint = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", userSignInDto.getUsername());
        formData.add("password", userSignInDto.getPassword());
        log.info("Logging in with username: {}, client: {}, realm: {}", userSignInDto.getUsername(), clientId, realm);


        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded");

        ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity(
                tokenEndPoint, new HttpEntity<>(formData, headers), TokenResponseDto.class
        );

        TokenResponseDto responseBody = response.getBody();

        if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
            return responseBody;
        } else {
            log.error("Keycloak failed to sign in, response: {}", response.getBody());
            throw new RuntimeException("Invalid username or password");
        }
    }

    public TokenResponseDto refreshAccessToken(String refreshToken) {
        String tokenEndPoint = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        log.info("Refreshing access token: clientId: {}", clientId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded");

        ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity(
                tokenEndPoint, new HttpEntity<>(formData, headers), TokenResponseDto.class
        );
        TokenResponseDto responseBody = response.getBody();

        if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
            return responseBody;
        } else {
            log.error("Keycloak failed to refresh token, response: {}", response.getBody());
            throw new RuntimeException("Failed to refresh token");
        }
    }

    public UserRepresentation createUser(UserCreateDto userCreateDto) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setEmail(userCreateDto.getEmail());
        newUser.setEmailVerified(true);
        newUser.setUsername(userCreateDto.getUsername());
        newUser.setFirstName(userCreateDto.getFirstName());
        newUser.setLastName(userCreateDto.getLastName());
        newUser.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userCreateDto.getPassword());
        credential.setTemporary(false);
        newUser.setCredentials(List.of(credential));

        // Создание пользователя
        Response response = keycloak.realm(realm).users().create(newUser);
        if (response.getStatus() == 201) {
            String location = response.getHeaderString("Location");
            String userId = location.substring(location.lastIndexOf('/') + 1);
            newUser.setId(userId);

            // 👉 Назначаем роль (например, "ADMIN")
            String roleName = userCreateDto.getRole();

            // Получаем доступ к user
            UserResource userResource = keycloak.realm(realm).users().get(userId);

            // Получаем роль из Realm
            RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();

            // Назначаем роль пользователю
            userResource.roles().realmLevel().add(List.of(role));

            return newUser;
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("Keycloak error: " + error);
        }
    }
}

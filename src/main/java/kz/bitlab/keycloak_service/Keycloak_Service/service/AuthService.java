package kz.bitlab.keycloak_service.Keycloak_Service.service;


import kz.bitlab.keycloak_service.Keycloak_Service.dto.TokenResponseDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserSignInDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

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


    //Логин пользователя
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
            log.error("Не удалось авторизоваться: {}", response.getBody());
            throw new RuntimeException("Неправильный логин или пароль");
        }
    }

    //Обновление Access Token
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


}
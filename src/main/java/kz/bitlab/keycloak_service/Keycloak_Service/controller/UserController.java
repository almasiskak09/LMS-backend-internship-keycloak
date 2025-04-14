package kz.bitlab.keycloak_service.Keycloak_Service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "name", jwt.getClaim("name"),
                "email", jwt.getClaim("email"),
                "username", jwt.getClaimAsString("preferred_username"),
                "roles", jwt.getClaimAsMap("realm_access").get("roles")
        );
    }
}
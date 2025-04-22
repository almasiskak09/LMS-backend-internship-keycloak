package kz.bitlab.keycloak_service.Keycloak_Service.restController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.TokenResponseDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserSignInDto;
import kz.bitlab.keycloak_service.Keycloak_Service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/auth")
@Tag(name = "Операции с ауентификацией и токеном")
public class AuthController {


    private final AuthService authService;

    @PostMapping(value = "/sign-in")
    @Operation(summary = "Авторизация пользователя с помощью логина и пароля")
    public ResponseEntity<TokenResponseDto> signIn (@RequestBody UserSignInDto userSignInDto) {
        return ResponseEntity.ok(authService.signIn(userSignInDto));
    }

    @PostMapping(value = "/refresh-token")
    @Operation(summary = "Обновление accessToken через refreshToken")
    public ResponseEntity<TokenResponseDto> refreshToken (String refreshToken) {
        System.out.println(refreshToken);
        return  ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }


}

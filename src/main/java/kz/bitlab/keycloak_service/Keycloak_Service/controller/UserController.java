package kz.bitlab.keycloak_service.Keycloak_Service.controller;

import kz.bitlab.keycloak_service.Keycloak_Service.dto.TokenResponseDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.UserSignInDto;
import kz.bitlab.keycloak_service.Keycloak_Service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/sign-in")
    public TokenResponseDto signIn (@RequestBody UserSignInDto userSignInDto) {
        return userService.signIn(userSignInDto);
    }

    @PostMapping(value = "/refresh-token")
    public TokenResponseDto refreshToken (String refreshToken) {
        System.out.println(refreshToken);
        return  userService.refreshAccessToken(refreshToken);

    }


}
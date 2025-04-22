package kz.bitlab.keycloak_service.Keycloak_Service.restController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserChangePasswordDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserCreateDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserResponseDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserUpdateDto;
import kz.bitlab.keycloak_service.Keycloak_Service.service.UserService;
import kz.bitlab.keycloak_service.Keycloak_Service.userUtil.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/user")
@Tag(name = "Операции с Пользователем", description = "Управление данными пользователей")
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех пользователей из Keycloak",description = "Права на получения списка пользователей только у админа")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(value = "/getUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователя по ID",description ="Права на получения пользователя только у админа" )
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание пользователя",description = "Права на создание пользователя только у админа")
    public ResponseEntity<UserCreateDto> createUser (@RequestBody UserCreateDto userCreateDto ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userCreateDto));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Поменять пароль у пользователя",description = "Доступ есть только у авторизованного пользователя")
    public ResponseEntity<String> changePassword(@RequestBody UserChangePasswordDto userChangePassword) {
        String currentUsername = UserUtil.getCurrentUsername();

        if(currentUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Could not identify the current user.");
        }
        try {
            userService.changePassword(currentUsername, userChangePassword.getNewPassword());
            return ResponseEntity.ok("Password changed successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing password: " + e.getMessage());
        }
    }

    @PutMapping(value = "/updateUser")
    @Operation(summary = "Обновление данных у пользователя",description = "Обновление email / firstName / lastName")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody UserUpdateDto userUpdateDto) {
        return ResponseEntity.ok(userService.updateUser(userUpdateDto));
    }

    @DeleteMapping(value = "/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удаление пользователя по ID",description = "Доступ к удалению только у админа")
    public ResponseEntity<String> deleteUserById(@PathVariable("id") String id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok("Пользователь успешно удален");
    }
}
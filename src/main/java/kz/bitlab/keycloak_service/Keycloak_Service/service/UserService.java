package kz.bitlab.keycloak_service.Keycloak_Service.service;

import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserCreateDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserResponseDto;
import kz.bitlab.keycloak_service.Keycloak_Service.dto.User.UserUpdateDto;
import kz.bitlab.keycloak_service.Keycloak_Service.exception.NotFoundException;
import kz.bitlab.keycloak_service.Keycloak_Service.userUtil.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;


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

    //Получить список всех пользователей
    public List<UserResponseDto> getAllUsers() {
        log.info("Получение всех пользователей");

        List<UserRepresentation>users = keycloak.realm(realm).users().list();
        return users.stream()
                .map(user -> {
                    String role = getUserPrimaryRole(user.getId());
                    return userToDto(user, role);
                })
                .collect(Collectors.toList());

    }

    //Получить пользователя по Id
    public UserResponseDto getUserById(String userId) {
        try {
            UserRepresentation user = keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .toRepresentation();
            String role = getUserPrimaryRole(userId);
            return userToDto(user, role);
        }catch (javax.ws.rs.NotFoundException e) {
            log.error("Пользователь по ID:{} не найден: ", userId);
            throw new NotFoundException("Пользователь по ID: " + userId+" не найден: " + e.getMessage());
        }
    }

    //Создать пользователя
    public UserCreateDto createUser(UserCreateDto userCreateDto) {

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

            return userCreateDto;
        } else {
            String error = response.readEntity(String.class);
            throw new RuntimeException("Keycloak error: " + error);
        }
    }

    // Обновить данные пользователя
    public UserResponseDto updateUser(UserUpdateDto userUpdateDto) {
        String username = UserUtil.getCurrentUsername();
        List<UserRepresentation> users = keycloak.realm(realm).users().search(username);

        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь не найден: " + username);
        }

        String userId = users.get(0).getId();
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        UserRepresentation existingUser = userResource.toRepresentation();

        // Устанавливаем ID обязательно!
        existingUser.setId(userId);

        if (userUpdateDto.getUsername() != null) {
            existingUser.setUsername(userUpdateDto.getUsername());
        }
        if (userUpdateDto.getFirstName() != null) {
            existingUser.setFirstName(userUpdateDto.getFirstName());
        }
        if (userUpdateDto.getLastName() != null) {
            existingUser.setLastName(userUpdateDto.getLastName());
        }
        if (userUpdateDto.getEmail() != null) {
            existingUser.setEmail(userUpdateDto.getEmail());
        }

        // Теперь обновляем пользователя
        userResource.update(existingUser);

        // Получаем роль для ответа
        List<RoleRepresentation> roles = userResource.roles().realmLevel().listEffective();
        String roleName = roles.isEmpty() ? null : roles.get(0).getName();

        return userToDto(existingUser, roleName);
    }

    //Изменить пароль пользователя
    public void changePassword(String username, String newPassword) {
        List<UserRepresentation> users = keycloak
                .realm(realm)
                .users()
                .search(username);

        if (users.isEmpty()) {
            log.error("Пользователь по никнейму: {} не найден: ", username);
            throw new RuntimeException("Пользователь по никнейму: "+username +" не найден " );
        }

        UserRepresentation user = users.get(0);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        keycloak
                .realm(realm)
                .users()
                .get(user.getId())
                .resetPassword(credential);

        log.info("Password changed successfully for username: {}", username);
    }

    //Удаление пользователя
    public void deleteUserById(String userId) {
        log.info("Удаление пользователя");

        RealmResource realmResource = keycloak.realm(realm);
        try {
            realmResource.users().get(userId).remove();
            log.debug("Пользователь с ID {} успешно удален", userId);
        } catch (Exception e) {
            log.error("Failed to delete user with ID {}", userId, e);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    //Возвращает роль пользователя
    public String getUserPrimaryRole(String userId) {
        List<RoleRepresentation> roles = keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .listEffective();

        if (roles.isEmpty()) {
            return null;
        }
        return roles.get(0).getName();
    }

    //Маппер с KeycloakUser в SpringDto
    public UserResponseDto userToDto(UserRepresentation user,String role){
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(role)
                .build();
    }

}

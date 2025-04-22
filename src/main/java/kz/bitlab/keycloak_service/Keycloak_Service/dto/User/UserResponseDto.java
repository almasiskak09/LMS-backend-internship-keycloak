package kz.bitlab.keycloak_service.Keycloak_Service.dto.User;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
}

package kz.bitlab.keycloak_service.Keycloak_Service.dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    private String email;
    private String username;
    private String firstName;
    private String lastName;

}

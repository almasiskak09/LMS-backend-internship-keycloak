package kz.bitlab.keycloak_service.Keycloak_Service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String password;
}



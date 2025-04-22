package kz.bitlab.keycloak_service.Keycloak_Service.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

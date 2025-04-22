FROM openjdk:21-oracle
LABEL maintainer="Almas"
COPY keycloak.jar keycloak-service.jar
ENTRYPOINT ["java", "-jar", "keycloak-service.jar"]

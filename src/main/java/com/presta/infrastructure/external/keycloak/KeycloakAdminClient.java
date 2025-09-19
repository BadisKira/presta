package com.presta.infrastructure.external.keycloak;


import com.presta.infrastructure.web.dtos.user.UpdatePasswordDto;
import com.presta.infrastructure.web.dtos.user.UpdateUserDto;
import jakarta.annotation.PostConstruct;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class KeycloakAdminClient {

    private Keycloak keycloak;


    private static final String SERVER_URL = "http://localhost:9090/";
    private static final String REALM = "presta-realm";
    private static final String CLIENT_ID = "admin-cli";
    private static final String USERNAME = "admin";  // ton user Keycloak admin
    private static final String PASSWORD = "admin";  // ton mot de passe Keycloak admin

    @PostConstruct
    public void init() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm("master") // ⚠️ toujours master pour se connecter en admin
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(CLIENT_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .build();
        System.out.println(("✅ KeycloakAdminClient initialisé avec realm {}" +  REALM));
    }


    public void deleteUser(String username) {
        List<UserRepresentation> users = keycloak.realm(REALM).users().search(username);
        if (users.isEmpty()) {
            System.out.println("⚠️ Utilisateur {} introuvable" + username);
            return;
        }
        String userId = users.get(0).getId();
        keycloak.realm(REALM).users().get(userId).remove();
    }

    public void assignRole(String userId, String roleName) {
        UserResource userResource = keycloak.realm(REALM).users().get(userId);

        RoleRepresentation role = keycloak.realm(REALM)
                .roles()
                .get(roleName)
                .toRepresentation();

        userResource.roles().realmLevel().add(Collections.singletonList(role));

        System.out.println("🎭 Rôle " + roleName + " attribué à userId=" + userId);
    }


    // 🎭 Retirer un rôle à un utilisateur
    public void removeRoleFromUser(String username, String roleName) {
        List<UserRepresentation> users = keycloak.realm(REALM).users().search(username);
        if (users.isEmpty()) {
            return;
        }
        String userId = users.get(0).getId();
        UserResource userResource = keycloak.realm(REALM).users().get(userId);

        RoleRepresentation role = keycloak.realm(REALM).roles().get(roleName).toRepresentation();
        userResource.roles().realmLevel().remove(Collections.singletonList(role));

    }


    public void updateUserById(String userId, UpdateUserDto dto) {
        UserResource userResource = keycloak.realm(REALM).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();

        if (dto.firstName() != null) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            user.setLastName(dto.lastName());
        }
        if (dto.enabled() != null) {
            user.setEnabled(dto.enabled());
        }

        userResource.update(user);
    }

    public void updateUserPasswordById(String userId, UpdatePasswordDto dto) {
        if (dto.newPassword() != null && !dto.newPassword().isBlank()) {
            UserResource userResource = keycloak.realm(REALM).users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setTemporary(false);
            credential.setValue(dto.newPassword());

            userResource.resetPassword(credential);
            System.out.println(" Mot de passe mis à jour pour userId=" + userId);
        }
    }


    public void banUser(String userId) {
        UserResource userResource = keycloak.realm(REALM).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();

        user.setEnabled(false);
        userResource.update(user);

        System.out.println(" Utilisateur " + userId + " banni (désactivé)");
    }

    public void unbanUser(String userId) {
        UserResource userResource = keycloak.realm(REALM).users().get(userId);
        UserRepresentation user = userResource.toRepresentation();

        user.setEnabled(true);
        userResource.update(user);

        System.out.println(" Utilisateur " + userId + " réactivé");
    }

}

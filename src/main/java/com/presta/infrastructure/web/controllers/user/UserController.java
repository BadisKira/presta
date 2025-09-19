package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.port.in.UserSyncPort;
import com.presta.domain.port.out.UserAuthenticationPort;
import com.presta.infrastructure.external.keycloak.KeycloakAdminClient;
import com.presta.infrastructure.web.dtos.user.UpdateUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserSyncPort userSyncPort;
    private final UserAuthenticationPort authPort;
    private final KeycloakAdminClient keycloakAdminClient;

    public UserController(UserSyncPort userSyncPort,
                          UserAuthenticationPort authPort,
                          KeycloakAdminClient keycloakAdminClient) {
        this.userSyncPort = userSyncPort;
        this.authPort = authPort;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @GetMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> syncUsers(@RequestParam  String role) {
         var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        keycloakAdminClient.assignRole(authUser.keycloakId().getValue().toString(),role);

        userSyncPort.syncUser(
                authUser.keycloakId(),
                authUser.profile(),
                authUser.contact(),
                List.of(role.equals("CLIENT") ? UserSyncPort.UserRole.CLIENT : UserSyncPort.UserRole.CONTRACTOR)
        );

        return ResponseEntity.ok("Sync completed for user: " + authUser.keycloakId());
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getMe() {
        var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        return ResponseEntity.ok(Map.of(
                "keycloakId", authUser.keycloakId().toString(),
                "profile", authUser.profile(),
                "contact", authUser.contact(),
                "roles", authUser.roles()
        ));
    }


    @DeleteMapping("/{username}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        keycloakAdminClient.deleteUser(username);
        return ResponseEntity.ok("Utilisateur supprimé : " + username);
    }

    // ✅ Ajouter un rôle à un utilisateur
    @PostMapping("/{username}/roles")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignRole(@PathVariable String id,
                                             @RequestBody Map<String, Object> payload) {
        String role = (String) payload.get("role");
        keycloakAdminClient.assignRole(id, role);
        return ResponseEntity.ok("Rôle " + role + " ajouté à " + id);
    }

    @DeleteMapping("/{username}/roles/{role}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> removeRole(@PathVariable String username,
                                             @PathVariable String role) {
        keycloakAdminClient.removeRoleFromUser(username, role);
        return ResponseEntity.ok("Rôle " + role + " retiré de " + username);
    }


    @PutMapping("/{id}")
//@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserKeycloak(@PathVariable String id,
                                                     @RequestBody UpdateUserDto dto) {
        keycloakAdminClient.updateUserById(id, dto);
        return ResponseEntity.ok("Utilisateur mis à jour : " + id);
    }

    // ✅ Bannir un utilisateur (désactiver son compte)
    @PutMapping("/{id}/ban")
//@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> banUser(@PathVariable String id) {
        keycloakAdminClient.banUser(id);
        return ResponseEntity.ok("Utilisateur banni : " + id);
    }

    // ✅ Débannir un utilisateur (réactiver son compte)
    @PutMapping("/{id}/unban")
//@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unbanUser(@PathVariable String id) {
        keycloakAdminClient.unbanUser(id);
        return ResponseEntity.ok("Utilisateur réactivé : " + id);
    }

}

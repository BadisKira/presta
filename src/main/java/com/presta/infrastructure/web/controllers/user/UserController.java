package com.presta.infrastructure.web.controllers.user;

import com.presta.domain.model.User;
import com.presta.domain.port.UserSyncPort;
import com.presta.domain.port.UserAuthenticationPort;
import com.presta.infrastructure.external.keycloak.KeycloakAdminClient;
import com.presta.infrastructure.persistence.adapters.UserRepositoryAdapter;
import com.presta.infrastructure.persistence.mapper.UserMapper;
import com.presta.infrastructure.web.dtos.user.UpdateUserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserSyncPort userSyncPort;
    private final UserAuthenticationPort authPort;
    private final KeycloakAdminClient keycloakAdminClient;
    private final UserRepositoryAdapter userRepositoryAdapter;
    private final UserMapper userMapper ;

    public UserController(UserSyncPort userSyncPort,
                          UserAuthenticationPort authPort,
                          KeycloakAdminClient keycloakAdminClient, UserRepositoryAdapter userRepositoryAdapter, UserMapper userMapper) {
        this.userSyncPort = userSyncPort;
        this.authPort = authPort;
        this.keycloakAdminClient = keycloakAdminClient;
        this.userRepositoryAdapter = userRepositoryAdapter;
        this.userMapper = userMapper;
    }

    @GetMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> syncUsers(@RequestParam("role")  String role) {
         var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        keycloakAdminClient.assignRole(authUser.keycloakId().getValue().toString(),role);

        userSyncPort.syncUser(
                authUser.keycloakId(),
                authUser.profile(),
                authUser.contact(),
                role.equals("CLIENT") ? UserSyncPort.UserRole.CLIENT : UserSyncPort.UserRole.CONTRACTOR);
        return ResponseEntity.ok("Sync completed for user: " + authUser.keycloakId());
    }




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




    @PutMapping("/{id}/ban")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> banUser(@PathVariable UUID id) {
        return ResponseEntity.ok(this.userRepositoryAdapter.deactivateUser(id));
    }

    @PutMapping("/{id}/unban")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> unbanUser(@PathVariable UUID id) {
        return ResponseEntity.ok(this.userRepositoryAdapter.activateUser(id));
    }

}

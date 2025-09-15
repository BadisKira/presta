package com.presta.infrastructure.web.controllers;

import com.presta.domain.port.in.UserSyncPort;
import com.presta.domain.port.out.UserAuthenticationPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserSyncPort userSyncPort;
    private final UserAuthenticationPort authPort;

    public UserController(UserSyncPort userSyncPort, UserAuthenticationPort authPort) {
        this.userSyncPort = userSyncPort;
        this.authPort = authPort;
    }

    @GetMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> testSync() {
        var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        userSyncPort.syncUser(
                authUser.keycloakId(),
                authUser.profile(),
                authUser.contact(),
                authUser.roles().stream()
                        .map(role -> "CLIENT".equals(role) ? UserSyncPort.UserRole.CLIENT : UserSyncPort.UserRole.CONTRACTOR)
                        .toList()
        );

        return ResponseEntity.ok("Sync completed for user: " + authUser.keycloakId());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> testMe() {
        var authUser = authPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        return ResponseEntity.ok(Map.of(
                "keycloakId", authUser.keycloakId().toString(),
                "profile", authUser.profile(),
                "contact", authUser.contact(),
                "roles", authUser.roles()
        ));
    }
}
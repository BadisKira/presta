package com.presta.infrastructure.external.keycloak;

import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.port.out.UserAuthenticationPort;
import org.springframework.stereotype.Service;

/**
 * Service utilitaire pour les controllers
 * Simplifie l'accès aux informations de l'utilisateur connecté
 */
@Service
public class CurrentUserService {

    private final UserAuthenticationPort authenticationPort;

    public CurrentUserService(UserAuthenticationPort authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    /**
     * Récupère l'ID Keycloak de l'utilisateur connecté
     * Lance une exception si pas connecté
     */
    public KeycloakUserId getCurrentUserId() {
        return authenticationPort.getCurrentAuthenticatedUser()
                .map(UserAuthenticationPort.AuthenticatedUser::keycloakId)
                .orElseThrow(() -> new SecurityException("User not authenticated"));
    }

    /**
     * Récupère l'utilisateur connecté complet
     * Lance une exception si pas connecté
     */
    public UserAuthenticationPort.AuthenticatedUser getCurrentUser() {
        return authenticationPort.getCurrentAuthenticatedUser()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
    }

    /**
     * Vérifie si l'utilisateur connecté a un rôle spécifique
     */
    public boolean hasRole(String role) {
        return authenticationPort.getCurrentAuthenticatedUser()
                .map(user -> user.roles().contains(role))
                .orElse(false);
    }

    /**
     * Vérifie si l'utilisateur connecté est un client
     */
    public boolean isClient() {
        return hasRole("CLIENT");
    }

    /**
     * Vérifie si l'utilisateur connecté est un contractor
     */
    public boolean isContractor() {
        return hasRole("CONTRACTOR");
    }
}
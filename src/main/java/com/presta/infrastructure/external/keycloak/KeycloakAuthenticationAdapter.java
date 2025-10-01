package com.presta.infrastructure.external.keycloak;

import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.port.UserAuthenticationPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter principal qui implémente UserAuthenticationPort
 * Fait le pont entre votre Domain et Keycloak
 * Utilise les services utilitaires pour extraire les données
 */
@Component
public class KeycloakAuthenticationAdapter implements UserAuthenticationPort {

    private final KeycloakAuthenticationService authenticationService;
    private final KeycloakJwtExtractor jwtExtractor;

    public KeycloakAuthenticationAdapter(
            KeycloakAuthenticationService authenticationService,
            KeycloakJwtExtractor jwtExtractor
    ) {
        this.authenticationService = authenticationService;
        this.jwtExtractor = jwtExtractor;
    }

    /**
     * Récupère l'utilisateur actuellement connecté avec toutes ses informations
     * Utilisé pour la synchronisation JIT et l'identification dans les controllers
     */
    /**
     * Construit un objet AuthenticatedUser à partir d'un token JWT
     * Gère les erreurs de validation des Value Objects
     */

    private Optional<UserAuthenticationPort.AuthenticatedUser> buildAuthenticatedUser(Jwt jwt) {
        try {
            // Extraction de l'ID utilisateur
            String userIdStr = jwtExtractor.extractUserId(jwt);
            KeycloakUserId keycloakId = KeycloakUserId.of(userIdStr);

            // Extraction du profil utilisateur
            String firstName = jwtExtractor.extractFirstName(jwt)
                    .orElseThrow(() -> new IllegalStateException("Token missing given_name"));
            String lastName = jwtExtractor.extractLastName(jwt)
                    .orElseThrow(() -> new IllegalStateException("Token missing family_name"));
            UserProfile profile = UserProfile.of(firstName, lastName);

            // Extraction des informations de contact
            String email = jwtExtractor.extractEmail(jwt)
                    .orElseThrow(() -> new IllegalStateException("Token missing email"));
            ContactInfo contact = ContactInfo.of(email);

            // Extraction des rôles métier
            List<String> roles = jwtExtractor.extractBusinessRoles(jwt);


            return Optional.of(new UserAuthenticationPort.AuthenticatedUser(keycloakId, profile, contact, roles));

        } catch (Exception e) {
            System.err.println("Error building authenticated user: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Récupère l'utilisateur actuellement connecté avec toutes ses informations
     * Utilisé pour la synchronisation JIT et l'identification dans les controllers
     */
    @Override
    public Optional<AuthenticatedUser> getCurrentAuthenticatedUser() {
        return authenticationService.getCurrentJwtToken()
                .flatMap(this::buildAuthenticatedUser);
    }

    /**
     * Récupère les rôles d'un utilisateur spécifique
     * Pour l'instant, ne fonctionne que pour l'utilisateur connecté
     * Pourrait être étendu avec l'API Admin Keycloak
     */
    @Override
    public List<String> getUserRoles(KeycloakUserId keycloakId) {
        // Vérifier si c'est l'utilisateur connecté
        Optional<String> currentUserId = authenticationService.getCurrentUserId();

        if (currentUserId.isPresent() &&
                currentUserId.get().equals(keycloakId.getValue().toString())) {

            return authenticationService.getCurrentJwtToken()
                    .map(jwtExtractor::extractBusinessRoles)
                    .orElse(List.of());
        }
        return List.of();
    }

    @Override
    public void getMe() {

    }


}
package com.presta.infrastructure.external.keycloak;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
public class KeycloakJwtExtractor {

    /**
     * Extrait l'ID utilisateur (subject) du token JWT
     */
    public String extractUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    /**
     * Extrait le prénom depuis le claim 'given_name'
     */
    public Optional<String> extractFirstName(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("given_name"));
    }

    /**
     * Extrait le nom de famille depuis le claim 'family_name'
     */
    public Optional<String> extractLastName(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("family_name"));
    }

    /**
     * Extrait l'email depuis le claim 'email'
     */
    public Optional<String> extractEmail(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsString("email"));
    }

    /**
     * Extrait les rôles métier depuis resource_access.account.roles
     * Filtre pour ne garder que CLIENT et CONTRACTOR
     */

    public List<String> extractBusinessRoles(Jwt jwt) {
        try {
            Object realmAccess = jwt.getClaim("realm_access");
            if (!(realmAccess instanceof Map<?, ?>)) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> realmMap = (Map<String, Object>) realmAccess;

            Object roles = realmMap.get("roles");
            if (!(roles instanceof List<?>)) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<String> rolesList = (List<String>) roles;

            return rolesList.stream()
                    .filter(role -> "CLIENT".equals(role) || "CONTRACTOR".equals(role))
                    .toList();

        } catch (Exception e) {
            return List.of();
        }
    }
    /**
     * Extrait tous les rôles (realm + resource) pour Spring Security
     * Utilisé par votre KeycloakJwtAuthenticationConverter existant
     */
    public List<String> extractAllRoles(Jwt jwt) {
        try {
            Object resourceAccess = jwt.getClaim("resource_access");
            if (!(resourceAccess instanceof Map<?, ?>)) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> resourceMap = (Map<String, Object>) resourceAccess;

            Object account = resourceMap.get("account");
            if (!(account instanceof Map<?, ?>)) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> accountMap = (Map<String, Object>) account;

            Object roles = accountMap.get("roles");
            if (!(roles instanceof List<?>)) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            List<String> rolesList = (List<String>) roles;

            return rolesList;

        } catch (Exception e) {
            return List.of();
        }
    }
}
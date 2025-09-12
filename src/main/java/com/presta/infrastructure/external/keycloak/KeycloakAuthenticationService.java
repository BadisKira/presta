package com.presta.infrastructure.external.keycloak;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service pour gérer l'authentification Spring Security avec Keycloak
 * Centralise l'accès au SecurityContext et l'extraction des tokens JWT
 */
@Service
public class KeycloakAuthenticationService {

    /**
     * Récupère l'authentification courante depuis le SecurityContext
     */
    public Optional<Authentication> getCurrentAuthentication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return Optional.ofNullable(authentication);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Récupère le token JWT depuis l'authentification courante
     */
    public Optional<Jwt> getCurrentJwtToken() {
        return getCurrentAuthentication()
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> (JwtAuthenticationToken) auth)
                .map(JwtAuthenticationToken::getToken);
    }

    /**
     * Vérifie si l'utilisateur est authentifié avec un token JWT valide
     */
    public boolean isAuthenticated() {
        return getCurrentJwtToken().isPresent();
    }

    /**
     * Récupère l'ID de l'utilisateur connecté (subject du JWT)
     */
    public Optional<String> getCurrentUserId() {
        return getCurrentJwtToken()
                .map(Jwt::getSubject);
    }
}
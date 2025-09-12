package com.presta.domain.model;

import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;

import java.util.Objects;
import java.util.UUID;

public record User(
        UUID id,
        KeycloakUserId keycloakId,
        UserProfile profile,
        ContactInfo contactInfo
) {
    public User {
        Objects.requireNonNull(id, "User ID cannot be null");
        Objects.requireNonNull(keycloakId, "Keycloak ID cannot be null");
        Objects.requireNonNull(profile, "User profile cannot be null");
        Objects.requireNonNull(contactInfo, "Contact info cannot be null");
    }

    public static User create(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contactInfo) {
        return new User(UUID.randomUUID(), keycloakId, profile, contactInfo);
    }

    public User updateProfile(UserProfile newProfile) {
        Objects.requireNonNull(newProfile, "User profile cannot be null");
        return new User(id, keycloakId, newProfile, contactInfo);
    }

    public User updateContactInfo(ContactInfo newContactInfo) {
        Objects.requireNonNull(newContactInfo, "Contact info cannot be null");
        return new User(id, keycloakId, profile, newContactInfo);
    }
}
package com.presta.domain.service;

import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.port.in.UserProfilePort;
import com.presta.domain.port.out.UserRepositoryPort;

import java.util.Optional;

public class UserProfileDomainService implements UserProfilePort {
    private final UserRepositoryPort userRepositoryPort;

    public UserProfileDomainService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<User> getUserProfile(KeycloakUserId keycloakId) {
        return userRepositoryPort.findUserByKeycloakId(keycloakId);
    }

    @Override
    public void updateUserProfile(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact) {
        User user = userRepositoryPort.findUserByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + keycloakId));

        // Règle métier : Mise à jour immutable des Value Objects
        User updatedUser = user.updateProfile(profile).updateContactInfo(contact);

        userRepositoryPort.saveUser(updatedUser);
    }

    // Méthode utilitaire pour la logique métier complexe
    public boolean isProfileComplete(KeycloakUserId keycloakId) {
        Optional<User> userOpt = userRepositoryPort.findUserByKeycloakId(keycloakId);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Règle métier : Vérifier si c'est un contractor avec profil complet
        Optional<Contractor> contractor = userRepositoryPort.findContractorById(user.id());
        if (contractor.isPresent()) {
            return validateContractorProfile(contractor.get());
        }

        // Pour un client, le profil User suffit
        return true;
    }

    private boolean validateContractorProfile(Contractor contractor) {
        // Règle métier : Un contractor doit avoir adresse et spécialité pour être complet
       // return contractor.address().isPresent() && contractor.speciality().isPresent();

        return false;
    }
}

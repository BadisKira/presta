package com.presta.domain.service;

import com.presta.domain.exception.ContractorProfileIncompleteException;
import com.presta.domain.exception.UserNotFoundException;
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
                .orElseThrow(() -> new UserNotFoundException(keycloakId.getValue()));

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
        return contractor.map(this::validateContractorProfile).orElse(true);

        // Pour un client, le profil User suffit
    }

    private boolean validateContractorProfile(Contractor contractor) {
        //        boolean complete = contractor.address(). && contractor.speciality().isEmpty();
        //        if (!complete) {
        //            throw new ContractorProfileIncompleteException();
        //        }
        //        return true;
        return false;

    }
}

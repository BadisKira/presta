package com.presta.domain.service;

import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.port.in.UserSyncPort;
import com.presta.domain.port.out.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class UserSyncDomainService implements UserSyncPort {
    private final UserRepositoryPort userRepositoryPort;

    public UserSyncDomainService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void syncUser(UserProfile profile, ContactInfo contact, List<UserRole> roles) {
        // Pour la synchronisation, on a besoin du keycloakId
        // Cette méthode sera appelée avec les infos d'un utilisateur authentifié
        throw new UnsupportedOperationException("Use syncUser(KeycloakUserId, UserProfile, ContactInfo, List<UserRole>) instead");
    }

    // Méthode de synchronisation principale
    public void syncUser(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact, List<UserRole> roles) {
        Optional<User> existingUser = userRepositoryPort.findUserByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            updateExistingUser(existingUser.get(), profile, contact);
        } else {
            createNewUser(keycloakId, profile, contact, roles);
        }
    }

    @Override
    public boolean userExists(KeycloakUserId keycloakId) {
        return userRepositoryPort.findUserByKeycloakId(keycloakId).isPresent();
    }

    private void updateExistingUser(User existingUser, UserProfile profile, ContactInfo contact) {
        // Règle métier : Synchronisation ne met à jour que les infos de profil de base
        // Les données métier spécifiques (adresse contractor, etc.) sont préservées
        User updatedUser = existingUser.updateProfile(profile).updateContactInfo(contact);
        userRepositoryPort.saveUser(updatedUser);
    }


    private void createNewUser(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact, List<UserRole> roles) {
        try{
            User newUser = User.create(keycloakId, profile, contact);
            User savedUser = userRepositoryPort.saveUser(newUser);
            createRoleSpecificAccounts(savedUser, roles);
        } catch (RuntimeException e) {
            System.out.println("**************************************");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void createRoleSpecificAccounts(User user, List<UserRole> roles) {
        if (roles.contains(UserRole.CLIENT)) {
            Client client = Client.create(user.id(),user);
            userRepositoryPort.saveClient(client);
        }

        System.out.println(user);
        System.out.println(roles.get(0));

        if (roles.contains(UserRole.CONTRACTOR)) {
            Contractor contractor = Contractor.create(user.id(),user,
                    "kaka pepe", "Adresse vide pour l'instant", null, null);
            Contractor c = userRepositoryPort.saveContractor(contractor);
        }


        if (roles.contains(UserRole.CLIENT) && roles.contains(UserRole.CONTRACTOR)) {
            throw new IllegalArgumentException("Un utilisateur ne peut pas être à la fois CLIENT et CONTRACTOR");
        }

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Un utilisateur doit avoir au moins un rôle");
        }
    }
}

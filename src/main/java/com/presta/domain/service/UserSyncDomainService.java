package com.presta.domain.service;

import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.port.UserSyncPort;
import com.presta.domain.port.UserRepositoryPort;

import java.util.Optional;

public class UserSyncDomainService implements UserSyncPort {
    private final UserRepositoryPort userRepositoryPort;

    public UserSyncDomainService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public void syncUser(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact, UserRole role) {
        Optional<User> existingUser = userRepositoryPort.findUserByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            updateExistingUser(existingUser.get(), profile, contact);
        } else {
            createNewUser(keycloakId, profile, contact, role);
        }
    }



    private void updateExistingUser(User existingUser, UserProfile profile, ContactInfo contact) {
        User updatedUser = existingUser.updateProfile(profile).updateContactInfo(contact);
        userRepositoryPort.saveUser(updatedUser);
    }


    private void createNewUser(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact, UserRole role) {
        try{
            User newUser = User.create(keycloakId, profile, contact);
            User savedUser = userRepositoryPort.saveUser(newUser);
            createRoleSpecificAccounts(savedUser, role);
        } catch (RuntimeException e) {
            System.out.println("**************************************");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void createRoleSpecificAccounts(User user, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Un utilisateur doit avoir au moins un rôle");
        }

        switch (role) {
            case CLIENT -> userRepositoryPort.saveClient(
                    Client.create(user.id(), user)
            );
            case CONTRACTOR -> userRepositoryPort.saveContractor(
                    Contractor.create(user.id(), user,user.profile().getFullName(), "", null, null)
            );
            default -> throw new IllegalArgumentException("Rôle non supporté : " + role);
        }
    }

}

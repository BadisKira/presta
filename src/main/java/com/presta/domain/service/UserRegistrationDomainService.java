package com.presta.domain.service;

import com.presta.domain.exception.UserAlreadyExistsException;
import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.port.in.UserRegistrationPort;
import com.presta.domain.port.out.UserRepositoryPort;

public class UserRegistrationDomainService implements UserRegistrationPort {

    private final UserRepositoryPort userRepositoryPort;

    public UserRegistrationDomainService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void registerUser(KeycloakUserId keycloakId, UserProfile profile, ContactInfo contact, UserRole role) {
        if (userRepositoryPort.findUserByKeycloakId(keycloakId).isPresent()) {
            throw new UserAlreadyExistsException(keycloakId.getValue());
        }

        // Créer l'utilisateur de base
        User user = User.create(keycloakId, profile, contact);
        User savedUser = userRepositoryPort.saveUser(user);

        createRoleBasedAccount(savedUser, role);
    }

    private void createRoleBasedAccount(User user, UserRole role) {
        switch (role) {
            case CLIENT -> {
                Client client = Client.create(user.id());
                userRepositoryPort.saveClient(client);
            }
            case CONTRACTOR -> {
                Contractor contractor = Contractor.create(user.id(), null, null, null, null);
                userRepositoryPort.saveContractor(contractor);
            }
        }
    }}

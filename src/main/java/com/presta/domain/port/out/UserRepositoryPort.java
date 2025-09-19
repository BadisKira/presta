package com.presta.domain.port.out;

import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.KeycloakUserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;


/**
 * Une interface clairement a changé car les principes solides ne sont pas respectés
 * */
public interface UserRepositoryPort {
    User saveUser(User user);
    Optional<User> findUserByKeycloakId(KeycloakUserId keycloakId);
    Optional<User> findUserById(UUID id);
    void deleteUser(UUID id);

    Client saveClient(Client client);
    Optional<Client> findClientById(UUID id);

    Contractor saveContractor(Contractor contractor);
    Optional<Contractor> findContractorById(UUID id);


    Page<Contractor> findContractors(String name, String speciality, Pageable pageable);
    Page<Client> findClients(String name, Pageable pageable);

}

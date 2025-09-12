package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.port.out.UserRepositoryPort;
import com.presta.infrastructure.persistence.entities.ClientEntity;
import com.presta.infrastructure.persistence.entities.ContractorEntity;
import com.presta.infrastructure.persistence.entities.UserEntity;
import com.presta.infrastructure.persistence.mapper.UserMapper;
import com.presta.infrastructure.persistence.repositories.JpaClientRepository;
import com.presta.infrastructure.persistence.repositories.JpaContractorRepository;
import com.presta.infrastructure.persistence.repositories.JpaUserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository userJpaRepository;
    private final JpaClientRepository clientJpaRepository;
    private final JpaContractorRepository contractorJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(
            JpaUserRepository userJpaRepository,
            JpaClientRepository clientJpaRepository,
            JpaContractorRepository contractorJpaRepository,
            UserMapper userMapper
    ) {
        this.userJpaRepository = userJpaRepository;
        this.clientJpaRepository = clientJpaRepository;
        this.contractorJpaRepository = contractorJpaRepository;
        this.userMapper = userMapper;
    }

    // ===== USER OPERATIONS =====

    @Override
    public User saveUser(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findUserByKeycloakId(KeycloakUserId keycloakId) {
        return userJpaRepository.findByKeycloakId(keycloakId.getValue())
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public void deleteUser(UUID id) {
        userJpaRepository.deleteById(id);
    }

    // ===== CLIENT OPERATIONS =====

    @Override
    public Client saveClient(Client client) {
        ClientEntity entity = userMapper.toEntity(client);
        ClientEntity savedEntity = clientJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Client> findClientById(UUID id) {
        return clientJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    // ===== CONTRACTOR OPERATIONS =====

    @Override
    public Contractor saveContractor(Contractor contractor) {
        ContractorEntity entity = userMapper.toEntity(contractor);
        ContractorEntity savedEntity = contractorJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Contractor> findContractorById(UUID id) {
        return contractorJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

}

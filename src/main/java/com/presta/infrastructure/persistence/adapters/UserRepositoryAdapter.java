package com.presta.infrastructure.persistence.adapters;

import com.presta.domain.exception.UserNotFoundException;
import com.presta.domain.model.Assignment;
import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.domain.port.out.UserRepositoryPort;
import com.presta.infrastructure.external.keycloak.KeycloakAdminClient;
import com.presta.infrastructure.persistence.entities.AssignmentEntity;
import com.presta.infrastructure.persistence.entities.ClientEntity;
import com.presta.infrastructure.persistence.entities.ContractorEntity;
import com.presta.infrastructure.persistence.entities.UserEntity;
import com.presta.infrastructure.persistence.mapper.user.UserMapper;
import com.presta.infrastructure.persistence.repositories.user.JpaClientRepository;
import com.presta.infrastructure.persistence.repositories.user.JpaContractorRepository;
import com.presta.infrastructure.persistence.repositories.user.JpaUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Transactional
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository userJpaRepository;
    private final JpaClientRepository clientJpaRepository;
    private final JpaContractorRepository contractorJpaRepository;
    private final UserMapper userMapper;
    private final EntityManager entityManager;
    private final KeycloakAdminClient keycloakAdminClient;


    public UserRepositoryAdapter(
            JpaUserRepository userJpaRepository,
            JpaClientRepository clientJpaRepository,
            JpaContractorRepository contractorJpaRepository,
            UserMapper userMapper, EntityManager entityManager, KeycloakAdminClient keycloakAdminClient
    ) {
        this.userJpaRepository = userJpaRepository;
        this.clientJpaRepository = clientJpaRepository;
        this.contractorJpaRepository = contractorJpaRepository;
        this.userMapper = userMapper;
        this.entityManager = entityManager;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    // ===== USER OPERATIONS =====

    @Override
    public User saveUser(User user) {
        UserEntity entity;

        // Vérifier si l'utilisateur existe déjà par son ID
        Optional<UserEntity> existingById = user.id() != null ?
                userJpaRepository.findById(user.id()) : Optional.empty();

        // Vérifier aussi par keycloakId au cas où
        Optional<UserEntity> existingByKeycloak =
                userJpaRepository.findByKeycloakId(user.keycloakId().getValue());

        if (existingById.isPresent()) {
            // UPDATE par ID
            entity = existingById.get();
            updateEntityFromUser(entity, user);
        } else if (existingByKeycloak.isPresent()) {
            // UPDATE par keycloakId (cas où l'ID domain ne correspond pas)
            entity = existingByKeycloak.get();
            updateEntityFromUser(entity, user);
        } else {
            // CREATE nouvelle entité
            entity = createEntityFromUser(user);
        }

        UserEntity saved = userJpaRepository.save(entity);
        entityManager.flush(); // Force la synchronisation avec la DB

        return toDomainUser(saved);
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
        // Supprimer dans l'ordre : Client/Contractor puis User
        if (clientJpaRepository.existsById(id)) {
            clientJpaRepository.deleteById(id);
        }
        if (contractorJpaRepository.existsById(id)) {
            contractorJpaRepository.deleteById(id);
        }
        userJpaRepository.deleteById(id);
    }

    @Override
    public User deactivateUser(UUID id){

        Optional<UserEntity> userEntity = userJpaRepository.findById(id);
        if(userEntity.isEmpty()){
            throw new UserNotFoundException(id);
        }
        try{
            this.keycloakAdminClient.banUser(userEntity.get().getKeycloakId().toString());
            return  toDomainUser(this.changeActivationAndReturn(id,false));
        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }

    }


    @Override
    public User activateUser(UUID id) {
        Optional<UserEntity> userEntity = userJpaRepository.findById(id);
        if(userEntity.isEmpty()){
            throw new UserNotFoundException(id);
        }
        try{
            this.keycloakAdminClient.unbanUser(userEntity.get().getKeycloakId().toString());
            return toDomainUser(this.changeActivationAndReturn(id,true));
        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public UserEntity changeActivationAndReturn(UUID userId, boolean active) {
        userJpaRepository.changeActivationState(userId, active);
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));
    }

    // ===== CLIENT OPERATIONS =====

    @Override
    @Transactional
    public Client saveClient(Client client) {
        // Le User DOIT exister avant de créer un Client
        UserEntity userEntity = userJpaRepository.findById(client.id())
                .orElseThrow(() -> new IllegalStateException(
                        "User doit être sauvegardé avant de créer un Client. User ID: " + client.id()));

        // Vérifier qu'il n'est pas déjà Contractor
        if (contractorJpaRepository.existsById(client.id())) {
            throw new IllegalStateException(
                    "User est déjà un Contractor, ne peut pas être aussi Client. User ID: " + client.id());
        }

        // Vérifier si le client existe déjà
        boolean clientExists = clientJpaRepository.existsById(client.id());

        ClientEntity clientEntity;
        if (clientExists) {
            // Client existe déjà, juste le récupérer
            clientEntity = clientJpaRepository.findById(client.id()).get();
        } else {
            // Créer un nouveau client
            clientEntity = new ClientEntity();
            clientEntity.setId(client.id());
            clientEntity.setUser(userEntity);

            // Utiliser persist pour une nouvelle entité au lieu de merge
            entityManager.persist(clientEntity);
        }

        entityManager.flush();

        return toDomainClient(clientEntity);
    }

    @Override
    public Optional<Client> findClientById(UUID id) {
        return clientJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    // ===== CONTRACTOR OPERATIONS =====

    @Override
    @Transactional
    public Contractor saveContractor(Contractor contractor) {
        UserEntity userEntity = userJpaRepository.findById(contractor.id())
                .orElseThrow(() -> new IllegalStateException(
                        "User doit être sauvegardé avant de créer un Contractor. User ID: " + contractor.id()));

        // Vérifier qu'il n'est pas déjà Client
        if (clientJpaRepository.existsById(contractor.id())) {
            throw new IllegalStateException(
                    "User est déjà un Client, ne peut pas être aussi Contractor. User ID: " + contractor.id());
        }

        // Vérifier si le contractor existe déjà
        boolean contractorExists = contractorJpaRepository.existsById(contractor.id());

        ContractorEntity contractorEntity = contractorExists
                ? contractorJpaRepository.findById(contractor.id()).orElseThrow()
                : new ContractorEntity();

        if (!contractorExists) {
            contractorEntity.setId(contractor.id());
            contractorEntity.setUser(userEntity);
        }

        AssignmentEntity assignmentEntity =null ;
        if(contractor.assignment() != null) {
            assignmentEntity = new AssignmentEntity();
            assignmentEntity.setId(contractor.assignment().id());
            assignmentEntity.setName(contractor.assignment().name());
            assignmentEntity.setDescription(contractor.assignment().description());
        }


        contractorEntity.setFullName(contractor.fullName());
        contractorEntity.setAddress(contractor.address());
        contractorEntity.setAssignment(assignmentEntity);
        contractorEntity.setSpeciality(contractor.speciality());

        if (contractorExists) {
            contractorJpaRepository.save(contractorEntity);
        } else {
            entityManager.persist(contractorEntity);
        }

        entityManager.flush();
        return toDomainContractor(contractorEntity);
    }

    @Override
    public Optional<Contractor> findContractorById(UUID id) {
        return contractorJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Page<Contractor> findContractors(String name, String speciality, Pageable pageable) {
        Specification<ContractorEntity> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("fullName")), "%" + name.toLowerCase() + "%"));
        }

        if (speciality != null && !speciality.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("speciality")), "%" + speciality.toLowerCase() + "%"));
        }

        Page<ContractorEntity> entityPage = contractorJpaRepository.findAll(spec, pageable);
        return entityPage.map(userMapper::toDomain);
    }

    @Override
    public Page<Client> findClients(String name, Pageable pageable) {
        Specification<ClientEntity> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) -> {
                Join<ClientEntity, UserEntity> userJoin = root.join("user");
                return cb.or(
                        cb.like(cb.lower(userJoin.get("firstName")), "%" + name.toLowerCase() + "%"),
                        cb.like(cb.lower(userJoin.get("lastName")), "%" + name.toLowerCase() + "%")
                );
            });
        }

        Page<ClientEntity> entityPage = clientJpaRepository.findAll(spec, pageable);
        return entityPage.map(userMapper::toDomain);
    }




    // ========== MÉTHODES DE CONVERSION ==========

    private UserEntity createEntityFromUser(User user) {
        return new UserEntity(
                //user.id(), // Utilise l'ID du domain
                user.keycloakId().getValue(),
                user.profile().getFirstName(),
                user.profile().getLastName(),
                user.contactInfo().email()
        );
    }

    private void updateEntityFromUser(UserEntity entity, User user) {
        entity.setKeycloakId(user.keycloakId().getValue());
        entity.setFirstName(user.profile().getFirstName());
        entity.setLastName(user.profile().getLastName());
        entity.setEmail(user.contactInfo().email());
    }

    private User toDomainUser(UserEntity entity) {
        KeycloakUserId keycloakId = new KeycloakUserId(entity.getKeycloakId());
        UserProfile profile = new UserProfile(entity.getFirstName(), entity.getLastName());
        ContactInfo contactInfo = new ContactInfo(entity.getEmail());

        return new User(entity.getId(), keycloakId, profile, contactInfo,entity.getIsActive());
    }

    private Client toDomainClient(ClientEntity entity) {
        User user = toDomainUser(entity.getUser());
        return Client.create(entity.getId(), user);
    }

    private Contractor toDomainContractor(ContractorEntity entity) {

        Assignment assignment =null ;
        if(entity.getAssignment() != null) {
            assignment =  new Assignment(entity.getAssignment().getId(),entity.getAssignment().getName(),entity.getAssignment().getDescription()) ;
        }



        User user = toDomainUser(entity.getUser());
        return Contractor.create(
                entity.getId(),
                user,
                entity.getFullName(),
                entity.getAddress(),assignment
               ,
                entity.getSpeciality()
        );
    }

}

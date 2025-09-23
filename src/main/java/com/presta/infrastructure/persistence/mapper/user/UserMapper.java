package com.presta.infrastructure.persistence.mapper.user;

import com.presta.domain.model.Assignment;
import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.infrastructure.persistence.entities.AssignmentEntity;
import com.presta.infrastructure.persistence.entities.ClientEntity;
import com.presta.infrastructure.persistence.entities.ContractorEntity;
import com.presta.infrastructure.persistence.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // ===== USER MAPPING =====

    public UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.id());
        userEntity.setKeycloakId(user.keycloakId().getValue());
        userEntity.setFirstName(user.profile().getFirstName());
        userEntity.setLastName(user.profile().getLastName());
        userEntity.setEmail(user.contactInfo().email());
        return userEntity;
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                KeycloakUserId.of(entity.getKeycloakId()),
                UserProfile.of(entity.getFirstName(), entity.getLastName()),
                ContactInfo.of(entity.getEmail()),
                entity.getIsActive()
        );
    }

    // ===== CLIENT MAPPING =====

    public ClientEntity toEntity(Client client) {
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(client.id());
        if (client.user() != null) {
            clientEntity.setUser(toEntity(client.user()));
        }
        return clientEntity;
    }

    public Client toDomain(ClientEntity entity) {
        return new Client(
                entity.getId(),
                entity.getUser() != null ? toDomain(entity.getUser()) : null
        );
    }

    // ===== CONTRACTOR MAPPING =====

    public ContractorEntity toEntity(Contractor contractor) {
        AssignmentEntity assignmentEntity = null;
        if (contractor.assignment() != null) {
            assignmentEntity = new AssignmentEntity();
            assignmentEntity.setId(contractor.assignment().id());
            assignmentEntity.setName(contractor.assignment().name());
            assignmentEntity.setDescription(contractor.assignment().description());
        }

        ContractorEntity contractorEntity = new ContractorEntity();
        contractorEntity.setId(contractor.id());
        contractorEntity.setFullName(contractor.fullName());
        contractorEntity.setAddress(contractor.address());
        contractorEntity.setAssignment(assignmentEntity);
        contractorEntity.setSpeciality(contractor.speciality());

        if (contractor.user() != null) {
            contractorEntity.setUser(toEntity(contractor.user()));
        }

        return contractorEntity;
    }

    public Contractor toDomain(ContractorEntity entity) {

        Assignment assignment = null;
        if(entity.getAssignment() != null) {
            assignment = new Assignment(
                    entity.getAssignment().getId(),
                    entity.getAssignment().getName(),
                    entity.getAssignment().getDescription());
        }

        return new Contractor(
                entity.getId(),
                entity.getUser() != null ? toDomain(entity.getUser()) : null,
                entity.getFullName(),
                entity.getAddress(),
                assignment,
                entity.getSpeciality()
        );
    }
}

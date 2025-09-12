package com.presta.infrastructure.persistence.mapper;

import com.presta.domain.model.Client;
import com.presta.domain.model.Contractor;
import com.presta.domain.model.User;
import com.presta.domain.model.valueobject.Address;
import com.presta.domain.model.valueobject.ContactInfo;
import com.presta.domain.model.valueobject.KeycloakUserId;
import com.presta.domain.model.valueobject.UserProfile;
import com.presta.infrastructure.persistence.entities.ClientEntity;
import com.presta.infrastructure.persistence.entities.ContractorEntity;
import com.presta.infrastructure.persistence.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // ===== USER MAPPING =====

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.id(),
                user.keycloakId().getValue(),
                user.profile().getFirstName(),
                user.profile().getLastName(),
                user.contactInfo().email()
        );
    }

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                KeycloakUserId.of(entity.getKeycloakId()),
                UserProfile.of(entity.getFirstName(), entity.getLastName()),
                ContactInfo.of(entity.getEmail())
        );
    }

    // ===== CLIENT MAPPING =====

    public ClientEntity toEntity(Client client) {
        return new ClientEntity(client.id());
    }

    public Client toDomain(ClientEntity entity) {
        return new Client(entity.getId());
    }

    // ===== CONTRACTOR MAPPING =====

    public ContractorEntity toEntity(Contractor contractor) {
        return new ContractorEntity(
                contractor.id(),
                contractor.fullName(),
                "L'adresse que tu dois modifier le code",
                //contractor.address().getFormattedAddress(),
                contractor.assignmentId(),
                contractor.speciality()
        );
    }

    public Contractor toDomain(ContractorEntity entity) {
        Address address = null;
        if (entity.getAddress() != null) {
            // Parsing simple de l'adresse - à améliorer selon votre format
            String[] parts = entity.getAddress().split(", ");
            if (parts.length >= 4) {
                address = Address.of(parts[0], parts[1], parts[2], parts[3]);
            }
        }

        return new Contractor(
                entity.getId(),
                entity.getFullName(),
                address,
                entity.getAssignmentId(),
                entity.getSpeciality()
        );
    }
}


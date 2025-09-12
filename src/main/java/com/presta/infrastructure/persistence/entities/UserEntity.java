package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "userApp", schema = "presta")
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private UUID keycloakId;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    // Constructeurs
    public UserEntity() {}

    public UserEntity(UUID id, UUID keycloakId, String firstName, String lastName, String email) {
        this.id = id;
        this.keycloakId = keycloakId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getKeycloakId() { return keycloakId; }
    public void setKeycloakId(UUID keycloakId) { this.keycloakId = keycloakId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

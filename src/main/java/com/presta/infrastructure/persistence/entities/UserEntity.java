package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(name = "user_app", schema = "presta")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "keycloak_id", unique = true, nullable = false, columnDefinition = "UUID")
    private UUID keycloakId;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "is_active")
    private boolean isActive;


    public UserEntity() {}

    public UserEntity(UUID keycloakId, String firstName, String lastName, String email) {
        this.keycloakId = keycloakId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }



    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(UUID keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserEntity that)) return false;
        return isActive == that.isActive && Objects.equals(id, that.id) && Objects.equals(keycloakId, that.keycloakId) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keycloakId, firstName, lastName, email, isActive);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", keycloakId=" + keycloakId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
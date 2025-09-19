package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(name = "client_account", schema = "presta")
public class ClientEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  // Partage l'ID avec UserEntity
    @JoinColumn(name = "id")
    private UserEntity user;

    // Constructeurs
    public ClientEntity() {}

    public ClientEntity(UserEntity user) {
        this.user = user;
        // L'ID sera automatiquement synchronisé grâce à @MapsId
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientEntity that = (ClientEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClientEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }
}
package com.presta.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "accountClient", schema = "presta")
public class ClientEntity {

    @Id
    private UUID id;

    public ClientEntity() {}

    public ClientEntity(UUID id) {
        this.id = id;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}